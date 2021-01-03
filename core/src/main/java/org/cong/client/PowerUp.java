package org.cong.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.cong.shared.protocol.PowerUpState;

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

  public void render(SpriteBatch batch, float delta) {
    sprite.rotate(delta * 100);
    sprite.draw(batch);
  }
}
