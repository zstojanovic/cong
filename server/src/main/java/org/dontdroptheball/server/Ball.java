package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.BallState;

import java.util.Optional;

public class Ball extends GameElement {
  static Repository<Ball> repo = new Repository<>(new Ball[Const.MAX_BALLS]);
  static short COLLISION_CODE = 1;

  float diameter = 0.5f;
  float velocity = 2.5f;

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
    body.setLinearVelocity(MathUtils.cos(direction) * velocity, MathUtils.sin(direction) * velocity);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    var body = world.createBody(bodyDef);
    var shape = new CircleShape();
    shape.setRadius(diameter/2);
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
      body.getPosition().x < -diameter || body.getPosition().x > (Const.WIDTH + diameter) ||
      body.getPosition().y < -diameter || body.getPosition().y > (Const.HEIGHT + diameter);
  }

  void freeze() {
    body.setActive(false);
  }

  void unfreeze() {
    body.setActive(true);
  }

  void dispose() {
    world.destroyBody(body);
    repo.remove(this);
  }
}
