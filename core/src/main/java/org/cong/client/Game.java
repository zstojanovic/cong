package org.cong.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;

public class Game extends com.badlogic.gdx.Game {
  final float WIDTH = 16;
  final float HEIGHT = 9;
  ClientConnectionManager connectionManager;
  TitleScreen title;
  GameScreen screen;
  Preferences preferences;
  Config config;
  Music music;

  @Override
  public void create() {
    preferences = Gdx.app.getPreferences("cong");
    config = new Config();
    config.load(Gdx.files.internal("config.properties"));
    music = Gdx.audio.newMusic(Gdx.files.internal("all_s_fair_in_love.ogg"));
    music.setLooping(true);
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
    try {
      preferences.flush();
    } catch (Exception e) { // this catch is here to workaround issue with incognito mode which appears in some cases
      Gdx.app.error(Game.class.getName(), e.getClass().getCanonicalName() + ": " + e.getMessage());
    }
  }

  @Override
  public void dispose() {
    connectionManager.dispose();
  }
}
