package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TitleScreen extends ScreenAdapter {
  Game game;
  SpriteBatch batch;
  OrthographicCamera camera;
  FitViewport viewport;
  Texture title;
  Stage stage;

  public TitleScreen(Game game) {
    this.game = game;
  }

  @Override
  public void show() {
    camera = new OrthographicCamera(game.WIDTH, game.HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new FitViewport(game.WIDTH, game.HEIGHT, camera);
    batch = new SpriteBatch();
    title = new Texture("titlescreen.png");
    stage = new Stage(new FitViewport(1280, 720));
    var skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    BitmapFont font = new BitmapFont(Gdx.files.internal("ui/nimbus-sans-l-bold-46.fnt"));
    TextField.TextFieldStyle style = new TextField.TextFieldStyle(
      font, Color.BLACK, skin.getDrawable("cursor"), skin.getDrawable("selection"), null);
    var nameField = new TextField("", style);
    nameField.setMaxLength(10);
    nameField.setAlignment(Align.center);
    nameField.setWidth(300);
    nameField.setPosition(800, 490);
    nameField.setText(game.getPlayerName());
    nameField.setCursorPosition(nameField.getText().length());
    nameField.setTextFieldListener((field, c) -> {
      if ((c == '\r' || c == '\n') && field.getText().length() > 2) {
        game.savePlayerName(field.getText());
        game.setScreen(new GameScreen(game));
      }
    });

    stage.addActor(nameField);
    stage.setKeyboardFocus(nameField);
    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void render(float delta) {
    stage.act(delta);
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();
    batch.draw(title, 0, 0, game.WIDTH, game.HEIGHT);
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
}
