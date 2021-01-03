package org.cong.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.cong.shared.protocol.BallState;

public class Ball {
  byte id;
  Sprite sprite;
  float diameter = 0.5f;

  public Ball(byte id, Texture texture) {
    this.id = id;
    sprite = new Sprite(texture);
    sprite.setSize(diameter, diameter);
    sprite.setOriginCenter();
  }

  public void setState(BallState state) {
    sprite.setCenter(state.x, state.y);
  }

  public void render(SpriteBatch batch) {
    sprite.draw(batch);
  }
}
