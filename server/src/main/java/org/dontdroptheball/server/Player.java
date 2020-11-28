package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const.Path;
import org.dontdroptheball.shared.protocol.KeyEvent;
import org.dontdroptheball.shared.protocol.PlayerState;

public class Player {
  byte index;
  String name;
  World world;
  float location;
  float maxSpeed = 5;
  float width = 1;
  float height = 0.3f;
  Body body;
  float currentSpeed = 0;

  public Player(byte index, String name, float location, World world) {
    this.index = index;
    this.name = name;
    this.location = location;
    this.world = world;

    body = createBody();
    updateBodyTransform();
  }

  public PlayerState getState() {
    return new PlayerState(index, location);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.KinematicBody;
    var body = world.createBody(bodyDef);
    var shape = new PolygonShape();
    shape.set(new float[]{-width/2,-height/2, -width/2,height/2, width/2,height/2, width/2,-height/2});
    var fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    body.createFixture(fixtureDef);
    shape.dispose();
    return body;
  }

  public void step(float delta) {
    location = location + (currentSpeed * delta);
    updateBodyTransform();
  }

  private void updateBodyTransform() {
    while (location < 0) location = location + Path.LENGTH;
    location = location % Path.LENGTH;
    if (location < Path.POINT1) {
      body.setTransform(location + Path.OFFSET, Path.OFFSET, 0);
    } else if (location >= Path.POINT1 && location < Path.POINT2) {
      body.setTransform(Path.WIDTH + Path.OFFSET, location - Path.POINT1 + Path.OFFSET, MathUtils.PI/2);
    } else if (location >= Path.POINT2 && location < Path.POINT3) {
      body.setTransform(Path.POINT3 - location + Path.OFFSET, Path.HEIGHT + Path.OFFSET, MathUtils.PI);
    } else {
      body.setTransform(Path.OFFSET, Path.LENGTH - location + Path.OFFSET, MathUtils.PI*1.5f);
    }
  }

  public void handleKeyEvent(KeyEvent keyEvent) {
    switch (keyEvent.code) {
      case LEFT_PRESSED:
        if (currentSpeed == 0) currentSpeed = maxSpeed; else currentSpeed = 0;
        break;
      case RIGHT_PRESSED:
        if (currentSpeed == 0) currentSpeed = -maxSpeed; else currentSpeed = 0;
        break;
      case LEFT_RELEASED:
        if (currentSpeed == 0) currentSpeed = -maxSpeed; else currentSpeed = 0;
        break;
      case RIGHT_RELEASED:
        if (currentSpeed == 0) currentSpeed = maxSpeed; else currentSpeed = 0;
        break;
    }
  }

  public void dispose() {
    world.destroyBody(body);
  }
}
