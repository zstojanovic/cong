package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.BallState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Ball {
  byte id;
  GameServer server;
  float diameter = 0.5f;
  Body body;
  float velocity = 2.5f;

  static short COLLISION_CODE = 1;
  private static Ball[] balls = new Ball[Const.MAX_BALLS];

  private Ball(byte id, GameServer server, Vector2 location) {
    this.id = id;
    this.server = server;
    body = createBody();
    body.setTransform(location, 0);
  }

  public static Ball create(GameServer server, Vector2 location) {
    byte newId = 0;
    while (newId < Const.MAX_BALLS && balls[newId] != null) newId++;
    if (newId == Const.MAX_BALLS) throw new RuntimeException("Too many balls");
    balls[newId] = new Ball(newId, server, location);
    return balls[newId];
  }

  public static List<Ball> all() {
    return Arrays.stream(balls).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public static Ball first() {
    return Arrays.stream(balls).filter(Objects::nonNull).findFirst().get();
  }

  public static long count() {
    return Arrays.stream(balls).filter(Objects::nonNull).count();
  }

  public BallState getState() {
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

  public void dispose() {
    balls[id] = null;
    server.world.destroyBody(body);
  }
}
