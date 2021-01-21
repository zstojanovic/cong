package org.cong.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.cong.shared.Const;
import org.cong.shared.Const.Path;
import org.cong.shared.protocol.*;

import java.util.Optional;

public class Paddle extends GameElement {
  static Repository<Paddle> repo = new Repository<>(new Paddle[Const.MAX_PADDLES]);

  static final short COLLISION_CODE = 2;
  public static final float MAX_VELOCITY = 5;
  static final float WIDTH = 1;
  static final float HEIGHT = 0.3f;

  private float location;
  private float currentVelocity = 0;
  private float speedFactor = 1;
  private float slowdownTimer;
  private float sizeIncreaseTimer;

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
    return new PaddleState(id, location, sizeIncreaseTimer > 0);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.KinematicBody;
    var body = world.createBody(bodyDef);
    body.setUserData(this);
    setShape(body, WIDTH);
    return body;
  }

  private void setShape(Body body, float width) {
    var shape = new PolygonShape();
    shape.set(new float[]{-width/2,-HEIGHT/2, -width/2,HEIGHT/2, width/2,HEIGHT/2, width/2,-HEIGHT/2});
    var fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.filter.categoryBits = COLLISION_CODE;
    body.createFixture(fixtureDef);
    shape.dispose();
  }

  void step(float delta) {
    location = location + (currentVelocity * speedFactor * delta);
    updateBodyTransform();
    if (slowdownTimer > 0) {
      slowdownTimer -= delta;
      if (slowdownTimer <= 0) {
        speedFactor = 1f;
      }
    }
    if (sizeIncreaseTimer > 0) {
      sizeIncreaseTimer -= delta;
      if (sizeIncreaseTimer <= 0) {
        body.getFixtureList().forEach(f -> body.destroyFixture(f));
        setShape(body, WIDTH);
      }
    }
  }

  void slowdown() {
    slowdownTimer = 5f;
    speedFactor = 0.5f;
  }

  void increaseSize() {
    sizeIncreaseTimer = 15f;
    body.getFixtureList().forEach(f -> body.destroyFixture(f));
    setShape(body, WIDTH * 1.5f);
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
        if (currentVelocity == 0) goLeft(); else stop();
        break;
      case RIGHT_PRESSED:
        if (currentVelocity == 0) goRight(); else stop();
        break;
      case LEFT_RELEASED:
        if (currentVelocity == 0) goRight(); else stop();
        break;
      case RIGHT_RELEASED:
        if (currentVelocity == 0) goLeft(); else stop();
        break;
    }
  }

  public void goLeft() {
    currentVelocity = MAX_VELOCITY;
  }

  public void goRight() {
    currentVelocity = -MAX_VELOCITY;
  }

  public void stop() {
    currentVelocity = 0;
  }

  public float location() {
    return location;
  }

  void dispose() {
    world.destroyBody(body);
    repo.remove(this);
    body = null;
  }
}
