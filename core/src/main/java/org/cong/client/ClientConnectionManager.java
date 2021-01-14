package org.cong.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.serialization.Transferable;
import org.cong.shared.protocol.*;

public class ClientConnectionManager extends WebSocketAdapter {
  Game game;
  String logTag = ClientConnectionManager.class.getName();
  ProtocolSerializer serializer = new ProtocolSerializer();
  WebSocket socket;
  int retryCount = 0;

  ClientConnectionManager(Game game) {
    this.game = game;
    initSocket();
  }

  private void initSocket() {
    socket = WebSockets.newSocket(game.config.get("serverUrl"));
    socket.addListener(this);
    socket.connect();
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        if (!socket.isOpen()) {
          retryCount++;
          Gdx.app.error(logTag, "Socket not open after timeout");
          socket.removeListener(ClientConnectionManager.this);
          socket.close(WebSocketCloseCode.NORMAL);
          if (retryCount < 10) {
            initSocket();
          } else {
            onFatalError();
          }
        }
      }
    }, 3);
  }

  void send(Transferable<?> transferable) {
    socket.send(serializer.serialize(transferable));
  }

  void send(String message) {
    socket.send(message);
  }

  @Override
  public boolean onOpen(WebSocket webSocket) {
    Gdx.app.debug(logTag, "Socket opened");
    send(new RecordStatsRequest());
    return FULLY_HANDLED;
  }

  @Override
  public boolean onMessage(WebSocket webSocket, byte[] packet) {
    var object = serializer.deserialize(packet);
    if (object instanceof NewPlayerResponse) {
      if (game.screen.isShown) game.screen.handleNewPlayerResponse((NewPlayerResponse)object);
    } else if (object instanceof PlayerNames) {
      if (game.screen.isShown) game.screen.handlePlayerNames((PlayerNames)object);
    } else if (object instanceof GameState) {
      if (game.screen.isShown) game.screen.setState((GameState)object);
    } else if (object instanceof ChatMessage) {
      if (game.screen.isShown) game.screen.receiveChatMessage((ChatMessage)object);
    } else if (object instanceof RecordStats) {
      game.title.handleRecordStats((RecordStats)object);
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
  public boolean onClose(WebSocket webSocket, WebSocketCloseCode code, String reason) {
    Gdx.app.error(logTag, "onClose: " + webSocket + " code: " + code.name() + " reason: " + reason);
    if (code != WebSocketCloseCode.NORMAL && retryCount > 10) onFatalError();
    return FULLY_HANDLED;
  }

  private void onFatalError() {
    if (game.screen.isShown) game.screen.onFatalError();
    game.title.onFatalError();
  }

  public void dispose() {
    socket.close();
  }
}
