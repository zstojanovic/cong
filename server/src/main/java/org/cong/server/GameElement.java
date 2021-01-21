package org.cong.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class GameElement extends Identifiable {
  protected World world;
  protected Body body;

  GameElement(byte id, World world) {
    super(id);
    this.world = world;
  }
}
