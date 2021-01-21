package org.cong.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.cong.shared.Const;
import org.cong.shared.protocol.PowerUpState;

import java.util.Optional;

public abstract class PowerUp extends GameElement {
  static Repository<PowerUp> repo = new Repository<>(new PowerUp[Const.MAX_POWER_UPS]);

  static final short COLLISION_CODE = 4;
  static final float DIAMETER = 0.25f;
  static final float VELOCITY = 1.5f;

  protected final PowerUpState.Type type;
  protected Optional<Paddle> paddle = Optional.empty();

  protected PowerUp(byte id, World world, PowerUpState.Type type) {
    super(id, world);
    this.type = type;
    body = createBody();
    var direction = MathUtils.random() * MathUtils.PI2;
    body.setTransform(Const.WIDTH/2, Const.HEIGHT/2, 0);
    body.setLinearVelocity(MathUtils.cos(direction) * VELOCITY, MathUtils.sin(direction) * VELOCITY);
  }

  PowerUpState getState() {
    return new PowerUpState(id, body.getPosition().x, body.getPosition().y, type);
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
    fixtureDef.filter.maskBits = Paddle.COLLISION_CODE;
    body.createFixture(fixtureDef);
    body.setUserData(this);
    shape.dispose();
    return body;
  }

  boolean dropped() {
    return
      (body.getPosition().x < -DIAMETER || body.getPosition().x > (Const.WIDTH + DIAMETER) ||
      body.getPosition().y < -DIAMETER || body.getPosition().y > (Const.HEIGHT + DIAMETER));
  }

  void collect(Paddle paddle) {
    this.paddle = Optional.of(paddle);
  }

  abstract void activate();

  void step(float delta) {
    if (paddle.isPresent()) {
      world.destroyBody(body);
      body = null;
      activate();
      dispose();
    } else if (dropped()) {
      dispose();
    }
  }

  void dispose() {
    if (body != null) world.destroyBody(body);
    repo.remove(this);
    body = null;
  }
}

class BallFreeze extends PowerUp {
  private BallFreeze(byte id, World world) {
    super(id, world, PowerUpState.Type.BALL_FREEZE);
  }

  static void create(World world) {
    repo.create(id -> new BallFreeze(id, world));
  }

  @Override
  void activate() {
    Ball.repo.stream().forEach(Ball::freeze);
  }
}

class ExtraBall extends PowerUp {
  private ExtraBall(byte id, World world) {
    super(id, world, PowerUpState.Type.EXTRA_BALL);
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
  }
}

class PaddleSlowdown extends PowerUp {
  private PaddleSlowdown(byte id, World world) {
    super(id, world, PowerUpState.Type.PADDLE_SLOWDOWN);
  }

  static void create(World world) {
    repo.create(id -> new PaddleSlowdown(id, world));
  }

  @Override
  void activate() {
    paddle.ifPresent(Paddle::slowdown);
  }
}

class PaddleGrowth extends PowerUp {
  private PaddleGrowth(byte id, World world) {
    super(id, world, PowerUpState.Type.PADDLE_GROWTH);
  }

  static void create(World world) {
    repo.create(id -> new PaddleGrowth(id, world));
  }

  @Override
  void activate() {
    paddle.ifPresent(Paddle::increaseSize);
  }
}