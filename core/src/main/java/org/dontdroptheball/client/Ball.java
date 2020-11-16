package org.dontdroptheball.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.dontdroptheball.shared.protocol.BallState;

public class Ball {
  Sprite sprite;
  float diameter = 0.5f;

  public Ball() {
    sprite = new Sprite(new Texture("ball.png"));
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
