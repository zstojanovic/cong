package org.dontdroptheball.client;

import com.badlogic.gdx.ScreenAdapter;

public class TitleScreen extends ScreenAdapter {
  DontDropTheBall game;

  public TitleScreen(DontDropTheBall game) {
    this.game = game;
  }

  @Override
  public void show() {
    game.setScreen(new GameScreen());
  }
}
