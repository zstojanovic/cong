package org.dontdroptheball.server;

import com.github.czyzby.websocket.serialization.Transferable;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServerConnectionManager extends WebSocketServer {
  Logger logger = LoggerFactory.getLogger(GameServer.class);
  static final int PORT = 2718;
  GameServer server;
  ProtocolSerializer serializer = new ProtocolSerializer();
  Map<WebSocket, Player> socketMap = new HashMap<>();
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

  ServerConnectionManager(GameServer server) {
    super(new InetSocketAddress(PORT));
    this.server = server;
    start();
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    logger.info(socket.getRemoteSocketAddress() + " new connection");
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    var player = socketMap.get(socket);
    if (player != null) {
      logger.info("Player " + player.index + " disconnected");
      server.disconnectPlayer(player);
    } else {
      logger.info(socket + " disconnected");
    }
  }

  @Override
  public void onMessage(WebSocket socket, String message) {
    var player = socketMap.get(socket);
    if (player != null) {
      var chatMessage = new ChatMessage(formatter.format(LocalTime.now()), player.index, message);
      broadcast(serializer.serialize(chatMessage));
    }
  }

  @Override
  public void onMessage(WebSocket socket, ByteBuffer message) {
    Object object = serializer.deserialize(message.array());
    var player = socketMap.get(socket);
    if (object instanceof NewPlayerRequest) {
      var newPlayer = server.createNewPlayer((NewPlayerRequest)object);
      newPlayer.ifPresent(p -> {
        socket.send(serializer.serialize(new NewPlayerResponse(p.index)));
        broadcast(new NewPlayerAnnouncement(p.index, p.name));
        socketMap.put(socket, p);
        logger.info("Player " + p.index + " created");
      });
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
}
