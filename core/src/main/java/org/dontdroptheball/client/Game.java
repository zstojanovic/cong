package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Game extends com.badlogic.gdx.Game {
  final float WIDTH = 16;
  final float HEIGHT = 9;
  ClientConnectionManager connectionManager;
  TitleScreen title;
  GameScreen screen;
  Preferences preferences;
  Config config;

  @Override
  public void create() {
    preferences = Gdx.app.getPreferences("dontdroptheball");
    config = new Config();
    config.load(Gdx.files.internal("config.properties"));
    title = new TitleScreen(this);
    screen = new GameScreen(this);
    setScreen(title);
    connectionManager = new ClientConnectionManager(this);
  }

  String getPlayerName() {
    return preferences.getString("playerName");
  }

  void savePlayerName(String name) {
    preferences.putString("playerName", name);
    preferences.flush();
  }

  @Override
  public void dispose() {
    connectionManager.dispose();
  }
}
