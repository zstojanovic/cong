package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import org.dontdroptheball.shared.GameState;
import org.dontdroptheball.shared.StateSerializer;

public class ClientConnectionManager extends WebSocketAdapter {
  String logTag = ClientConnectionManager.class.getName();
  GameScreen gameScreen;
  StateSerializer serializer = new StateSerializer();
  WebSocket socket;

  ClientConnectionManager(GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    socket = WebSockets.newSocket("ws://localhost:2222");
    socket.addListener(this);
    socket.connect();
  }

  void send(Object object) {
    socket.send(serializer.serialize(object));
  }

  @Override
  public boolean onMessage(WebSocket webSocket, byte[] packet) {
    var object = serializer.deserialize(packet);
    if (object instanceof GameState) {
      gameScreen.setState((GameState)object);
    } else {
      Gdx.app.error(logTag, "Unexpected object: " + object.getClass().getCanonicalName());
    }
    return FULLY_HANDLED;
  }

  @Override
  public boolean onError(WebSocket webSocket, Throwable error) {
    Gdx.app.error(logTag, "Error: " + (error == null ? "null" : error.getMessage()));
    return FULLY_HANDLED;
  }

  @Override
  public boolean onOpen(WebSocket webSocket) {
    // TODO
    return FULLY_HANDLED;
  }

  @Override
  public boolean onClose(WebSocket webSocket, WebSocketCloseCode code, String reason) {
    // TODO
    return FULLY_HANDLED;
  }
}