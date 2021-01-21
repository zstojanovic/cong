package org.cong.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.cong.shared.Const;
import org.cong.shared.protocol.BallState;

import java.util.Optional;

public class Ball extends GameElement {
  public static Repository<Ball> repo = new Repository<>(new Ball[Const.MAX_BALLS]);

  static final short COLLISION_CODE = 1;
  static final float DIAMETER = 0.5f;
  static final float VELOCITY = 2.5f;

  private float freezeTimer;

  private Ball(byte id, World world) {
    super(id, world);
    body = createBody();
    body.setTransform(new Vector2(Const.WIDTH/2, Const.HEIGHT/2), 0);
  }

  static Optional<Ball> create(World world) {
    return repo.create(id -> new Ball(id, world));
  }

  BallState getState() {
    return new BallState(id, body.getPosition().x, body.getPosition().y);
  }

  void startCountdown() {
    body.setTransform(Const.WIDTH/2, Const.HEIGHT/2, 0);
    body.setLinearVelocity(0, 0);
  }

  void startPlaying(Vector2 target) {
    startPlaying(target.sub(body.getPosition()).angleRad() + MathUtils.random(-0.04f, 0.04f));
  }

  void startPlaying(float direction) {
    body.setLinearVelocity(MathUtils.cos(direction) * VELOCITY, MathUtils.sin(direction) * VELOCITY);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    var body = world.createBody(bodyDef);
    var shape = new CircleShape();
    shape.setRadius(DIAMETER / 2);
    var fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.friction = 0;
    fixtureDef.restitution = 1;
    fixtureDef.density = 0.04f;
    fixtureDef.filter.categoryBits = COLLISION_CODE;
    fixtureDef.filter.maskBits = (short)(Paddle.COLLISION_CODE | Ball.COLLISION_CODE);
    body.createFixture(fixtureDef);
    body.setUserData(this);
    shape.dispose();
    return body;
  }

  boolean dropped() {
    return
      body.getPosition().x < -DIAMETER || body.getPosition().x > (Const.WIDTH + DIAMETER) ||
      body.getPosition().y < -DIAMETER || body.getPosition().y > (Const.HEIGHT + DIAMETER);
  }

  void step(float delta) {
    var diff = Math.abs(body.getLinearVelocity().len() - VELOCITY);
    if (diff > 0.05f) {
      body.setLinearVelocity(body.getLinearVelocity().setLength(VELOCITY));
    }
    if (freezeTimer > 0) {
      freezeTimer -= delta;
      if (freezeTimer <= 0) {
        body.setActive(true);
      }
    }
  }

  void freeze() {
    freezeTimer = 5f;
    body.setActive(false);
  }

  void dispose() {
    world.destroyBody(body);
    repo.remove(this);
    body = null;
  }
}
