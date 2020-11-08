package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.dontdroptheball.shared.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GameScreen extends ScreenAdapter {
  float WORLD_WIDTH = 16f;
  float WORLD_HEIGHT = 9f;

  ClientConnectionManager connectionManager;
  SpriteBatch batch;
  OrthographicCamera camera;
  FitViewport viewport;
  Texture background;
  Texture panel;
  Ball ball = new Ball();
  Player[] players = new Player[Arena.MAX_PLAYERS];
  Texture[] paddleTextures = new Texture[Arena.MAX_PLAYERS];

  void setState(GameState state) {
    ball.setState(state.ballState);
    var stateIndexes = state.playerStates.stream().map(s -> s.index).collect(Collectors.toList());
    var missing =
      Arrays.stream(players).filter(p -> p != null && !stateIndexes.contains(p.index)).collect(Collectors.toList());
    for (Player p: missing) {
      players[p.index] = null;
    }
    for (PlayerState playerState: state.playerStates) {
      if (players[playerState.index] == null) {
        players[playerState.index] =
          new Player(playerState.index, playerState.location, paddleTextures[playerState.index]);
      } else {
        players[playerState.index].setState(playerState);
      }
    }
  }

  @Override
  public void show() {
    connectionManager = new ClientConnectionManager(this);

    camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    batch = new SpriteBatch();
    background = new Texture("background.png");
    panel = new Texture("panel.png");
    for (int i = 0; i < Arena.MAX_PLAYERS; i++) {
      paddleTextures[i] = new Texture("paddle" + i + ".png");
    }

    Gdx.input.setInputProcessor(new InputHandler());
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
  }

  @Override
  public void resize(int width, int height) {
    camera.viewportWidth = WORLD_WIDTH;
    camera.viewportHeight = WORLD_HEIGHT;
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
}
