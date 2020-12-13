package org.dontdroptheball.server;

import com.github.czyzby.websocket.serialization.Transferable;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
    setTcpNoDelay(true);
    start();
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    if (getConnections().size() > Const.MAX_PLAYERS) {
      logger.warn(socket.getRemoteSocketAddress() + " requesting connection but limit reached");
      socket.close();
    } else {
      logger.info(socket.getRemoteSocketAddress() + " new connection");
    }
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    var player = socketMap.get(socket);
    if (player != null) {
      logger.info("Player " + player.id + " disconnected");
      server.disconnectPlayer(player);
    } else {
      logger.info(socket + " disconnected");
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
    if (object instanceof NewPlayerRequest) {
      var newPlayer = server.createNewPlayer((NewPlayerRequest)object, socket);
      if (newPlayer.isPresent()) {
        socketMap.put(socket, newPlayer.get());
        logger.info("Player " + newPlayer.get().id + " created");
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
