package org.dontdroptheball.client;

import com.badlogic.gdx.*;
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
  Ball ball = new Ball();
  Player[] players = new Player[Arena.MAX_PLAYERS];
  Texture[] paddleTextures = new Texture[Arena.MAX_PLAYERS];
  Stage stage;
  TextArea chatArea;
  Label scoreLabel;
  Label recordLabel;

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
    for (int i = 0; i < Arena.MAX_PLAYERS; i++) {
      paddleTextures[i] = new Texture("paddle" + i + ".png");
    }

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
    ball.render(batch);
    for (Player player: players) {
      if (player != null) player.render(batch);
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
    ball.setState(state.ballState);
    scoreLabel.setText(String.valueOf((int)state.ballState.playTimer));
    recordLabel.setText(String.valueOf((int)state.ballState.record));
    var stateIndexes = state.playerStates.stream().map(s -> s.index).collect(Collectors.toList());
    var missing =
      Arrays.stream(players).filter(p -> p != null && !stateIndexes.contains(p.index)).collect(Collectors.toList());
    for (Player p: missing) {
      players[p.index] = null;
    }
    for (PlayerState p: state.playerStates) {
      if (players[p.index] == null) {
        players[p.index] =
          new Player(p.index, "Player" + p.index, p.location, paddleTextures[p.index]);
      } else {
        players[p.index].setState(p);
      }
    }
  }

  void receiveChatMessage(ChatMessage message) {
    // TODO prune old, out of frame messages
    var m = "(" + message.timestamp + ") " + players[message.playerIndex].name + ":\n" + message.text;
    chatArea.appendText(m);
  }

  void handleNewPlayerAnnouncement(NewPlayerAnnouncement announcement) {
    if (players[announcement.index] == null) {
      players[announcement.index] =
        new Player(announcement.index, announcement.name, 0, paddleTextures[announcement.index]);
    } else {
      players[announcement.index].name = announcement.name;
    }
  }

  void handleNewPlayerResponse(NewPlayerResponse response) {
    // TODO
  }
}
