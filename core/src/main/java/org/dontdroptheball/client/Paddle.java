package org.dontdroptheball.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.dontdroptheball.shared.Const.Path;
import org.dontdroptheball.shared.protocol.PaddleState;

public class Paddle {
  String logTag = Paddle.class.getName();
  byte index;
  float width = 1;
  float height = 0.3f;
  float location;
  Sprite sprite;

  public Paddle(byte index, float location, Texture texture) {
    this.index = index;
    this.location = location;

    sprite = new Sprite(texture);
    sprite.setSize(width, height);
    sprite.setOriginCenter();
    updateSprite();
  }

  public void setState(PaddleState state) {
    location = state.location;
    if (state.index != index) Gdx.app.error(logTag, "Illegal state sent from server");
    updateSprite();
  }

  public void updateSprite() {
    while (location < 0) location = location + Path.LENGTH;
    location = location % Path.LENGTH;
    if (location < Path.POINT1) {
      sprite.setCenter(location + Path.OFFSET, Path.OFFSET);
      sprite.setRotation(0);
    } else if (location >= Path.POINT1 && location < Path.POINT2) {
      sprite.setCenter(Path.WIDTH + Path.OFFSET, location - Path.POINT1 + Path.OFFSET);
      sprite.setRotation(90);
    } else if (location >= Path.POINT2 && location < Path.POINT3) {
      sprite.setCenter(Path.POINT3 - location + Path.OFFSET, Path.HEIGHT + Path.OFFSET);
      sprite.setRotation(180);
    } else {
      sprite.setCenter(Path.OFFSET, Path.LENGTH - location + Path.OFFSET);
      sprite.setRotation(270);
    }
  }

  public void render(SpriteBatch batch) {
    sprite.draw(batch);
  }
}
