package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.PowerUpState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class PowerUp {
  enum Stage { EGG, MATURITY };

  Stage stage = Stage.EGG;
  byte id;
  GameServer server;
  Body body;
  float diameter = 0.25f;
  float velocity = 1.5f;
  float timer = 5f;
  Optional<Paddle> paddle = Optional.empty();

  static short COLLISION_CODE = 4;
  private static PowerUp[] powerUps = new PowerUp[Const.MAX_BALLS];

  PowerUp(GameServer server) {
    this.id = getNewId();
    this.server = server;
    body = createBody();
    body.setUserData(this);
    body.setTransform(Const.WIDTH/2, Const.HEIGHT/2, 0);
    var direction = MathUtils.random() * MathUtils.PI2;
    body.setLinearVelocity(MathUtils.cos(direction) * velocity, MathUtils.sin(direction) * velocity);
    powerUps[id] = this;
  }

  byte getNewId() {
    byte newId = 0;
    while (newId < Const.MAX_BALLS && powerUps[newId] != null) newId++;
    if (newId == Const.MAX_BALLS) throw new RuntimeException("Too many power-ups");
    return newId;
  }

  public static List<PowerUp> all() {
    return Arrays.stream(powerUps).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public static List<PowerUp> withBodies() {
    return Arrays.stream(powerUps).filter(p -> p != null && p.stage == Stage.EGG).collect(Collectors.toList());
  }

  public PowerUpState getState() {
    return new PowerUpState(id, body.getPosition().x, body.getPosition().y);
  }

  private Body createBody() {
    var bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    var body = server.world.createBody(bodyDef);
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
      server.world.destroyBody(body);
      activate();
    } else if (dropped()) {
      dispose();
    }
    if (timer < 0) deactivate();
  }

  public void dispose() {
    powerUps[id] = null;
  }
}

class BallFreeze extends PowerUp {
  BallFreeze(GameServer server) {
    super(server);
  }

  @Override
  void activate() {
    Ball.all().forEach(Ball::freeze);
  }

  @Override
  void deactivate() {
    Ball.all().forEach(Ball::unfreeze);
    dispose();
  }
}

class ExtraBall extends PowerUp {
  ExtraBall(GameServer server) {
    super(server);
  }

  @Override
  void activate() {
    if (Ball.count() < Const.MAX_BALLS) {
      var newBall = Ball.create(server, new Vector2(Const.WIDTH/2, Const.HEIGHT/2));
      var p = Paddle.random();
      if (p.isPresent()) {
        newBall.startPlaying(p.get().body.getPosition());
      } else {
        newBall.startPlaying(Ball.first().body.getAngle() + MathUtils.PI);
      }
    }
    deactivate();
  }
}

class PaddleSlowdown extends PowerUp {
  PaddleSlowdown(GameServer server) {
    super(server);
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