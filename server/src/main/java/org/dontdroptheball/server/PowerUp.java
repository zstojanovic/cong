package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.PowerUpState;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class PowerUp extends GameElement {
  static Repository<PowerUp> repo = new Repository<>(new PowerUp[Const.MAX_POWER_UPS]);
  static short COLLISION_CODE = 4;

  Optional<Paddle> paddle = Optional.empty();
  float diameter = 0.25f;
  float velocity = 1.5f;
  float timer = 5f;

  enum Stage { EGG, MATURITY };
  Stage stage = Stage.EGG;

  protected PowerUp(byte id, World world) {
    super(id, world);
    body = createBody();
    var direction = MathUtils.random() * MathUtils.PI2;
    body.setTransform(Const.WIDTH/2, Const.HEIGHT/2, 0);
    body.setLinearVelocity(MathUtils.cos(direction) * velocity, MathUtils.sin(direction) * velocity);
  }

  static Stream<PowerUp> withBodies() {
    return repo.stream().filter(p -> p.stage == Stage.EGG);
  }

  PowerUpState getState() {
    return new PowerUpState(id, body.getPosition().x, body.getPosition().y);
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
    fixtureDef.filter.maskBits = Paddle.COLLISION_CODE;
    body.createFixture(fixtureDef);
    body.setUserData(this);
    shape.dispose();
    return body;
  }

  boolean dropped() {
    return stage == Stage.EGG &&
      (body.getPosition().x < -diameter || body.getPosition().x > (Const.WIDTH + diameter) ||
      body.getPosition().y < -diameter || body.getPosition().y > (Const.HEIGHT + diameter));
  }

  void trigger(Paddle paddle) {
    this.paddle = Optional.of(paddle);
  }

  abstract void activate();

  void deactivate() {
    dispose();
  }

  void step(float delta) {
    if (stage == Stage.MATURITY) {
      timer -= delta;
    } else if (paddle.isPresent()) {
      stage = Stage.MATURITY;
      world.destroyBody(body);
      body = null;
      activate();
    } else if (dropped()) {
      dispose();
    }
    if (timer < 0) deactivate();
  }

  void dispose() {
    if (body != null) world.destroyBody(body);
    repo.remove(this);
  }
}

class BallFreeze extends PowerUp {
  private BallFreeze(byte id, World world) {
    super(id, world);
  }

  static void create(World world) {
    repo.create(id -> new BallFreeze(id, world));
  }

  @Override
  void activate() {
    Ball.repo.stream().forEach(Ball::freeze);
  }

  @Override
  void deactivate() {
    Ball.repo.stream().forEach(Ball::unfreeze);
    dispose();
  }
}

class ExtraBall extends PowerUp {
  private ExtraBall(byte id, World world) {
    super(id, world);
  }

  static void create(World world) {
    repo.create(id -> new ExtraBall(id, world));
  }

  @Override
  void activate() {
    var ball = Ball.create(world);
    ball.ifPresent(newBall -> {
      var p = Paddle.repo.random();
      if (p.isPresent()) {
        newBall.startPlaying(p.get().body.getPosition());
      } else {
        newBall.startPlaying(Ball.repo.first().body.getAngle() + MathUtils.PI);
      }
    });
    deactivate();
  }
}

class PaddleSlowdown extends PowerUp {
  private PaddleSlowdown(byte id, World world) {
    super(id, world);
  }

  static void create(World world) {
    repo.create(id -> new PaddleSlowdown(id, world));
  }

  @Override
  void activate() {
    paddle.ifPresent(Paddle::slowdown);
  }

  @Override
  void deactivate() {
    paddle.ifPresent(Paddle::fullSpeed);
    dispose();
  }
}