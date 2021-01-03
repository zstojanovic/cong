package org.cong.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.cong.shared.Const.Path;
import org.cong.shared.protocol.PaddleState;

public class Paddle {
  String logTag = Paddle.class.getName();
  byte index;
  float width = 1;
  float height = 0.3f;
  float location;
  boolean sizeIncreased;
  Sprite sprite;

  public Paddle(byte index, float location, Texture texture, boolean sizeIncreased) {
    this.index = index;
    this.location = location;
    this.sizeIncreased = sizeIncreased;

    sprite = new Sprite(texture);
    sprite.setSize(width, height);
    sprite.setOriginCenter();
    updateSprite();
  }

  // TODO I guess setState/updateSprite/render could be optimized, but it doesn't seem to cause problems right now

  public void setState(PaddleState state) {
    location = state.location;
    sizeIncreased = state.sizeIncreased;
    if (state.index != index) Gdx.app.error(logTag, "Illegal state sent from server");
  }

  public void updateSprite() {
    sprite.setSize(sizeIncreased ? width * 1.5f : width, height);
    sprite.setOriginCenter();
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
    updateSprite();
    sprite.draw(batch);
  }
}
