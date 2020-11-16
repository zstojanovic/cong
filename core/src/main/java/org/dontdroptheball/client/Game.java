package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Game extends com.badlogic.gdx.Game {
  final float WIDTH = 16;
  final float HEIGHT = 9;
  Preferences preferences;

  @Override
  public void create() {
    preferences = Gdx.app.getPreferences("dontdroptheball");
    setScreen(new TitleScreen(this));
  }

  String getPlayerName() {
    return preferences.getString("playerName");
  }

  void savePlayerName(String name) {
    preferences.putString("playerName", name);
    preferences.flush();
  }
}
