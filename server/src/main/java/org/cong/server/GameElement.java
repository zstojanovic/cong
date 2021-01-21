package org.cong.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class GameElement extends Identifiable {
  protected World world;
  protected Body body;

  GameElement(byte id, World world) {
    super(id);
    this.world = world;
  }

  public Vector2 linearVelocity() {
    return body.getLinearVelocity();
  }

  public Vector2 position() {
    return body.getPosition();
  }
}
