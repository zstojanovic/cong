package org.dontdroptheball.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.dontdroptheball.shared.protocol.PowerUpState;

public class PowerUp {
  byte id;
  Sprite sprite;
  float diameter = 0.25f;

  public PowerUp(byte id, Texture texture) {
    this.id = id;
    sprite = new Sprite(texture);
    sprite.setSize(diameter, diameter);
    sprite.setOriginCenter();
  }

  public void setState(PowerUpState state) {
    sprite.setCenter(state.x, state.y);
  }

  public void render(SpriteBatch batch) {
    sprite.draw(batch);
  }
}