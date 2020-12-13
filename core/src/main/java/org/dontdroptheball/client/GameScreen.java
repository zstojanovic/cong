package org.dontdroptheball.client;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.dontdroptheball.shared.*;
import org.dontdroptheball.shared.protocol.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GameScreen extends ScreenAdapter {
  Game game;
  ClientConnectionManager connectionManager;
  SpriteBatch batch;
  OrthographicCamera camera;
  FitViewport viewport;
  Texture background;
  Texture panel;
  Ball[] balls = new Ball[Const.MAX_BALLS];
  Player[] players = new Player[Const.MAX_PLAYERS];
  Paddle[] paddles = new Paddle[Const.MAX_PADDLES];
  PowerUp[] powerUps = new PowerUp[Const.MAX_POWER_UPS];
  Texture[] paddleTextures = new Texture[Const.MAX_PADDLES];
  Texture[] powerUpTextures = new Texture[PowerUpState.Type.values().length];
  Texture ballTexture;
  Stage stage;
  TextArea chatArea;
  Label scoreLabel;
  Label recordLabel;
  Sound bounce, drop;

  GameScreen(Game game) {
    this.game = game;
  }

  @Override
  public void show() {
    camera = new OrthographicCamera(game.WIDTH, game.HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new FitViewport(game.WIDTH, game.HEIGHT, camera);
    batch = new SpriteBatch();
    background = new Texture("background.png");
    panel = new Texture("panel.png");
    for (int i = 0; i < Const.MAX_PADDLES; i++) {
      paddleTextures[i] = new Texture("paddle" + i + ".png");
    }
    ballTexture = new Texture("ball.png");
    for (int i = 0; i < powerUpTextures.length; i++) {
      powerUpTextures[i] = new Texture("powerup" + i + ".png");
    }
    bounce = Gdx.audio.newSound(Gdx.files.internal("tock.mp3"));
    drop = Gdx.audio.newSound(Gdx.files.internal("bottle-break.mp3"));

    stage = new Stage(new FitViewport(1280, 720));
    var skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    var messageArea = new TextArea("", skin);
    messageArea.setSize(400, 60);
    messageArea.setPosition(880, 0);
    messageArea.setMaxLength(80);
    messageArea.setFocusTraversal(false);
    messageArea.setBlinkTime(1);
    messageArea.setTextFieldListener((field, c) -> {
      if ((c == '\r' || c == '\n') && !field.getText().isEmpty()) {
        connectionManager.send(field.getText());
        field.setText("");
      }
    });

    chatArea = new TextArea("", skin);
    chatArea.setSize(400, 524);
    chatArea.setPosition(880, 60);
    chatArea.setFocusTraversal(false);
    chatArea.setDisabled(true);

    var style = new Label.LabelStyle(new BitmapFont(Gdx.files.internal("ui/nimbus-sans-l-bold-24.fnt")), Color.BLACK);
    scoreLabel = new Label("", style);
    scoreLabel.setAlignment(Align.center);
    scoreLabel.setWidth(88);
    scoreLabel.setPosition(896, 647);

    recordLabel = new Label("", style);
    recordLabel.setAlignment(Align.center);
    recordLabel.setWidth(88);
    recordLabel.setPosition(896, 615);

    stage.addActor(scoreLabel);
    stage.addActor(recordLabel);
    stage.addActor(messageArea);
    stage.addActor(chatArea);
    stage.setKeyboardFocus(messageArea);

    var multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(new InputHandler());
    multiplexer.addProcessor(stage);

    Gdx.input.setInputProcessor(multiplexer);
    connectionManager = new ClientConnectionManager(game, this);
  }

  @Override
  public void render(float delta) {
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();
    batch.draw(background, 0, 0, 16, 9);
    for (Ball ball: balls) {
      if (ball != null) ball.render(batch);
    }
    for (Paddle paddle : paddles) {
      if (paddle != null) paddle.render(batch);
    }
    for (PowerUp p: powerUps) {
      if (p != null) p.render(batch);
    }
    batch.draw(panel, 11, 0, 5, 9);
    batch.end();
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    camera.viewportWidth = game.WIDTH;
    camera.viewportHeight = game.HEIGHT;
    camera.update();
    viewport.update(width, height);
  }

  private class InputHandler extends InputAdapter {
    @Override
    public boolean keyDown(int keyCode) {
      if (keyCode == Input.Keys.LEFT) connectionManager.send(new KeyEvent(KeyEvent.Code.LEFT_PRESSED));
      if (keyCode == Input.Keys.RIGHT) connectionManager.send(new KeyEvent(KeyEvent.Code.RIGHT_PRESSED));
      return true;
    }
    @Override
    public boolean keyUp(int keyCode) {
      if (keyCode == Input.Keys.LEFT) connectionManager.send(new KeyEvent(KeyEvent.Code.LEFT_RELEASED));
      if (keyCode == Input.Keys.RIGHT) connectionManager.send(new KeyEvent(KeyEvent.Code.RIGHT_RELEASED));
      return true;
    }
  }

  @Override
  public void hide() {
    connectionManager.dispose();
  }

  void setState(GameState state) {
    scoreLabel.setText(String.valueOf((int)state.playTimer));
    recordLabel.setText(String.valueOf((int)state.record));
    setPaddleStates(state.paddleStates);
    setBallStates(state.ballStates);
    setPowerUpStates(state.powerUpStates);
    if (state.bounce) bounce.play();
    if (state.drop) drop.play();
  }

  void setPaddleStates(PaddleState[] paddleStates) {
    var stateIndexes = Arrays.stream(paddleStates).map(s -> s.index).collect(Collectors.toList());
    var missing =
      Arrays.stream(paddles).filter(p -> p != null && !stateIndexes.contains(p.index)).collect(Collectors.toList());
    for (Paddle p: missing) {
      paddles[p.index] = null;
    }
    for (PaddleState p: paddleStates) {
      if (paddles[p.index] == null) {
        paddles[p.index] = new Paddle(p.index, p.location, paddleTextures[p.index]);
      } else {
        paddles[p.index].setState(p);
      }
    }
  }

  void setBallStates(BallState[] ballStates) {
    var stateIndexes = Arrays.stream(ballStates).map(s -> s.id).collect(Collectors.toList());
    var missing =
      Arrays.stream(balls).filter(p -> p != null && !stateIndexes.contains(p.id)).collect(Collectors.toList());
    for (Ball b: missing) {
      balls[b.id] = null;
    }
    for (BallState b: ballStates) {
      if (balls[b.id] == null) balls[b.id] = new Ball(b.id, ballTexture);
       balls[b.id].setState(b);
    }
  }

  void setPowerUpStates(PowerUpState[] powerUpStates) {
    var stateIndexes = Arrays.stream(powerUpStates).map(s -> s.id).collect(Collectors.toList());
    var missing =
      Arrays.stream(powerUps).filter(p -> p != null && !stateIndexes.contains(p.id)).collect(Collectors.toList());
    for (PowerUp p: missing) {
      powerUps[p.id] = null;
    }
    for (PowerUpState p: powerUpStates) {
      if (powerUps[p.id] == null) powerUps[p.id] = new PowerUp(p.id, powerUpTextures[p.type.ordinal()]);
      powerUps[p.id].setState(p);
    }
  }

  void receiveChatMessage(ChatMessage message) {
    String m;
    if (message.isServerMessage()) {
      m = "[" + message.timestamp + "] >>> " + message.text;
    } else {
      m = "(" + message.timestamp + ") " + players[message.playerId].name + ":\n" + message.text;
    }
    var lines = (chatArea.getText() + m).split("\\r?\\n");
    var newText = new StringBuilder();
    var skipCount = Math.max(0, lines.length - (Const.MESSAGE_LIMIT * 2));
    Arrays.stream(lines).skip(skipCount).forEach(s -> newText.append(s).append("\n"));
    chatArea.setText(newText.toString());
    chatArea.setCursorPosition(newText.length());
  }

  void handlePlayerNames(PlayerNames playerNames) {
    for (byte i = 0; i < Const.MAX_PLAYERS; i++) {
      var name = playerNames.names[i];
      if (name != null) {
        if (players[i] == null) {
          players[i] = new Player(i, name);
        } else {
          players[i].name = name;
        }
      }
    }
  }

  void handleNewPlayerResponse(NewPlayerResponse response) {
    // TODO make some use of response.index or remove it from protocol
    for (ChatMessage message: response.messages) {
      receiveChatMessage(message);
    }
  }
}
