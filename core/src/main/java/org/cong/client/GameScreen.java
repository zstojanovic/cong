package org.cong.client;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.cong.shared.*;
import org.cong.shared.protocol.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GameScreen extends ScreenAdapter {
  Game game;
  SpriteBatch batch;
  ShakeableCamera camera;
  FitViewport viewport;
  Texture background;
  Image errorMessage;
  boolean errorOccurred;
  Ball[] balls = new Ball[Const.MAX_BALLS];
  Player[] players = new Player[Const.MAX_PLAYERS];
  Paddle[] paddles = new Paddle[Const.MAX_PADDLES];
  PowerUp[] powerUps = new PowerUp[Const.MAX_POWER_UPS];
  Texture[] paddleTextures = new Texture[Const.MAX_PADDLES];
  Texture[] powerUpTextures = new Texture[PowerUpState.Type.values().length];
  Texture ballTexture;
  Stage stage;
  TextArea chatArea;
  String chatText = "";
  Label scoreLabel;
  Label recordLabel;
  Sound bounce, drop, collect;
  boolean isShown;

  GameScreen(Game game) {
    this.game = game;
  }

  @Override
  public void show() {
    game.music.play();
    camera = new ShakeableCamera(game.WIDTH, game.HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new FitViewport(game.WIDTH, game.HEIGHT, camera);
    batch = new SpriteBatch();
    background = new Texture("background.png");
    for (int i = 0; i < Const.MAX_PADDLES; i++) {
      paddleTextures[i] = new Texture("paddle" + i + ".png");
    }
    ballTexture = new Texture("ball.png");
    for (int i = 0; i < powerUpTextures.length; i++) {
      powerUpTextures[i] = new Texture("powerup" + i + ".png");
    }
    bounce = Gdx.audio.newSound(Gdx.files.internal("glass_002.mp3"));
    drop = Gdx.audio.newSound(Gdx.files.internal("bottlebreak2.mp3"));
    collect = Gdx.audio.newSound(Gdx.files.internal("confirmation_003.mp3"));

    stage = new Stage(new FitViewport(1280, 720));
    var skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    var panel = new Image(new Texture("panel.png"));
    panel.setScale(0.5f);
    panel.setPosition(880, 0);

    var messageArea = new TextArea("", skin);
    messageArea.setSize(400, 60);
    messageArea.setPosition(880, 0);
    messageArea.setMaxLength(80);
    messageArea.setBlinkTime(1);
    messageArea.setTextFieldListener((field, c) -> {
      if ((c == '\r' || c == '\n') && !field.getText().isEmpty() && !errorOccurred) {
        game.connectionManager.send(field.getText());
        field.setText("");
      }
    });

    chatArea = new TextArea(chatText, skin);
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

    errorMessage = new Image(new Texture("errormessage.png"));
    errorMessage.setScale(0.5f);
    errorMessage.setPosition(164, 8);
    errorMessage.setVisible(errorOccurred);

    stage.addActor(panel);
    stage.addActor(scoreLabel);
    stage.addActor(recordLabel);
    stage.addActor(messageArea);
    stage.addActor(chatArea);
    stage.addActor(errorMessage);
    stage.setKeyboardFocus(messageArea);

    var multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(new InputHandler());
    multiplexer.addProcessor(stage);
    Gdx.input.setInputProcessor(multiplexer);

    isShown = true;
    game.connectionManager.send(new NewPlayerRequest(game.getPlayerName()));
  }

  @Override
  public void render(float delta) {
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();
    batch.draw(background, 0, 0, 16, 9);
    for (PowerUp p: powerUps) {
      if (p != null) p.render(batch, delta);
    }
    for (Ball ball: balls) {
      if (ball != null) ball.render(batch);
    }
    for (Paddle paddle : paddles) {
      if (paddle != null) paddle.render(batch);
    }
    batch.end();
    chatArea.setText(chatText);
    chatArea.setCursorPosition(chatText.length());
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
      if (keyCode == Input.Keys.F2) game.music.setVolume(1 - game.music.getVolume());
      if (keyCode == Input.Keys.LEFT) game.connectionManager.send(new KeyEvent(KeyEvent.Code.LEFT_PRESSED));
      if (keyCode == Input.Keys.RIGHT) game.connectionManager.send(new KeyEvent(KeyEvent.Code.RIGHT_PRESSED));
      return true;
    }
    @Override
    public boolean keyUp(int keyCode) {
      if (keyCode == Input.Keys.LEFT) game.connectionManager.send(new KeyEvent(KeyEvent.Code.LEFT_RELEASED));
      if (keyCode == Input.Keys.RIGHT) game.connectionManager.send(new KeyEvent(KeyEvent.Code.RIGHT_RELEASED));
      return true;
    }
  }

  void setState(GameState state) {
    scoreLabel.setText(String.valueOf((int)state.playTimer));
    recordLabel.setText(String.valueOf((int)state.record));
    setPaddleStates(state.paddleStates);
    setBallStates(state.ballStates);
    setPowerUpStates(state.powerUpStates);
    if (state.drop) {
      drop.play();
      camera.shake();
    }
    if (state.bounce) bounce.play();
    if (state.collect) collect.play();
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
        paddles[p.index] = new Paddle(p.index, p.location, paddleTextures[p.index], p.sizeIncreased);
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
    var lines = (chatText + m).split("\\r?\\n");
    var newText = new StringBuilder();
    var skipCount = Math.max(0, lines.length - (Const.MESSAGE_LIMIT * 2));
    Arrays.stream(lines).skip(skipCount).forEach(s -> newText.append(s).append("\n"));
    chatText = newText.toString();
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

  void onFatalError() {
    errorOccurred = true;
    if (errorMessage != null) errorMessage.setVisible(true);
  }
}
