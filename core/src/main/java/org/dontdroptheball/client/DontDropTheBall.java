package org.dontdroptheball.client;

import com.badlogic.gdx.Game;

public class DontDropTheBall extends Game {

  @Override
  public void create() {
    setScreen(new TitleScreen(this));
  }
}
