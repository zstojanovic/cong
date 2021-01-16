package org.cong.server;

import com.github.czyzby.websocket.serialization.Transferable;
import org.cong.shared.Const;
import org.cong.shared.protocol.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class ServerConnectionManager extends WebSocketServer {
  Logger logger = LoggerFactory.getLogger(GameServer.class);
  static final int PORT = 2718;
  GameServer server;
  ProtocolSerializer serializer = new ProtocolSerializer();
  Map<WebSocket, Player> socketMap = new HashMap<>();

  ServerConnectionManager(GameServer server) {
    super(new InetSocketAddress(PORT));
    this.server = server;
    handleSSL();
    setTcpNoDelay(true);
    setConnectionLostTimeout(15);
    start();
  }

  void handleSSL() {
    var keystore = System.getProperty("cong.keystore");
    if (keystore != null) {
      try {
        var password = "password";
        var store = KeyStore.getInstance("JKS");
        store.load(new FileInputStream(keystore), password.toCharArray());
        var keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(store, password.toCharArray());
        var trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(store);
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        logger.info("Loaded SSL certificate from " + keystore);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    if (getConnections().size() > Const.MAX_PLAYERS) {
      logger.warn(socket.getRemoteSocketAddress() + " requesting connection but limit reached");
      socket.close();
    } else {
      logger.info(socket.getRemoteSocketAddress() + " new connection, socket " + socket);
    }
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    logger.info(socket + " closing with code: " + code + ", remote: " + remote + ", reason: " + reason);
    disconnect(socket);
  }

  void disconnect(WebSocket socket) {
    var player = socketMap.get(socket);
    if (player != null) {
      logger.info(player + " disconnected");
      server.disconnectPlayer(player);
      socketMap.remove(socket);
    }
  }

  @Override
  public void onMessage(WebSocket socket, String message) {
    var player = socketMap.get(socket);
    if (player != null) {
      server.handleMessage(player, message);
    } else {
      logger.error("Unknown socket (" + socket + ") sent message: " + message);
    }
  }

  @Override
  public void onMessage(WebSocket socket, ByteBuffer message) {
    Object object = serializer.deserialize(message.array());
    var player = socketMap.get(socket);
    if (object instanceof RecordStatsRequest) {
      server.handleRecordStatsRequest(socket);
    } else if (object instanceof NewPlayerRequest) {
      var newPlayer = server.createNewPlayer((NewPlayerRequest)object, socket);
      if (newPlayer.isPresent()) {
        socketMap.put(socket, newPlayer.get());
        logger.info(newPlayer.get() + " created");
      } else {
        logger.error("Too many players");
      }
    } else if (player == null) {
      logger.error(socket.getRemoteSocketAddress() +
        " received object from connection without Player" + object.getClass().getCanonicalName());
    } else if (object instanceof KeyEvent) {
      server.handleKeyEvent(player, (KeyEvent)object);
    } else {
      logger.error(socket.getRemoteSocketAddress() + " unexpected object: " + object.getClass().getCanonicalName());
    }
  }

  @Override
  public void onError(WebSocket socket, Exception e) {
    var stringWriter = new StringWriter();
    var writer = new PrintWriter(stringWriter);
    if (socket != null) writer.print(socket.getRemoteSocketAddress());
    writer.print(" error ");
    e.printStackTrace(writer);
    logger.error(stringWriter.toString());
    disconnect(socket);
  }

  @Override
  public void onStart() {
    setConnectionLostTimeout(0);
    setConnectionLostTimeout(100);
  }

  void broadcast(Transferable<?> transferable) {
    broadcast(serializer.serialize(transferable));
  }

  void send(WebSocket socket, Transferable<?> transferable) {
    socket.send(serializer.serialize(transferable));
  }
}
