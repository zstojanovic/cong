package org.dontdroptheball.server;

import org.dontdroptheball.shared.GameState;
import org.dontdroptheball.shared.KeyEvent;
import org.dontdroptheball.shared.StateSerializer;
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
  static final int PORT = 2222;
  GameServer server;
  StateSerializer serializer = new StateSerializer();
  Map<WebSocket, Player> socketMap = new HashMap<>();

  ServerConnectionManager(GameServer server) {
    super(new InetSocketAddress(PORT));
    this.server = server;
    start();
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    logger.info(socket.getRemoteSocketAddress() + " new connection");
    var player = server.createNewPlayer();
    player.ifPresent(p -> socketMap.put(socket, p));
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    logger.info(socket + " disconnected");
  }

  @Override
  public void onMessage(WebSocket socket, String message) {
    logger.error(socket.getRemoteSocketAddress() + " unexpected message: " + message);
  }

  @Override
  public void onMessage(WebSocket socket, ByteBuffer message) {
    Object object = serializer.deserialize(message.array());
    if (object instanceof KeyEvent) {
      server.handleKeyEvent(socketMap.get(socket), (KeyEvent)object);
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

  void broadcastState(GameState state) {
    broadcast(serializer.serialize(state));
  }
}
