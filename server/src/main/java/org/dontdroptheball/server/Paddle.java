package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.Const.Path;
import org.dontdroptheball.shared.protocol.*;

import java.util.Optional;

public class Paddle extends GameElement {
  static Repository<Paddle> repo = new Repository<>(new Paddle[Const.MAX_PADDLES]);
  static short COLLISION_CODE = 2;

  float location;
  float maxSpeed = 5;
  float width = 1;
  float height = 0.3f;
  float currentSpeed = 0;
  float speedFactor = 1;

  private Paddle(byte id, World world) {
    super(id, world);
    body = createBody();
    this.location = MathUtils.random(0f, Path.LENGTH);
    updateBodyTransform();
  }

  static Optional<Paddle> create(World world) {
    return repo.create(id -> new Paddle(id, world));
  }

  PaddleState getState() {
    return new PaddleState(id, location);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.KinematicBody;
    var body = world.createBody(bodyDef);
    var shape = new PolygonShape();
    shape.set(new float[]{-width/2,-height/2, -width/2,height/2, width/2,height/2, width/2,-height/2});
    var fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.filter.categoryBits = COLLISION_CODE;
    body.createFixture(fixtureDef);
    body.setUserData(this);
    shape.dispose();
    return body;
  }

  void step(float delta) {
    location = location + (currentSpeed * speedFactor * delta);
    updateBodyTransform();
  }

  void slowdown() {
    speedFactor = 0.5f;
  }

  void fullSpeed() {
    speedFactor = 1f;
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

  void handleKeyEvent(KeyEvent keyEvent) {
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

  void dispose() {
    world.destroyBody(body);
    repo.remove(this);
  }
}
