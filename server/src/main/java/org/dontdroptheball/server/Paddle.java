package org.dontdroptheball.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.Const.Path;
import org.dontdroptheball.shared.protocol.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Paddle {
  byte index;
  World world;
  float location;
  float maxSpeed = 5;
  float width = 1;
  float height = 0.3f;
  Body body;
  float currentSpeed = 0;

  private static Paddle[] paddles = new Paddle[Const.MAX_PADDLES];

  public static Optional<Paddle> create(World world) {
    byte newIndex = 0;
    while (newIndex < Const.MAX_PADDLES && paddles[newIndex] != null) newIndex++;
    if (newIndex == Const.MAX_PADDLES) return Optional.empty();
    var paddle = new Paddle(newIndex, MathUtils.random(0f, Path.LENGTH), world);
    paddles[newIndex] = paddle;
    return Optional.of(paddle);
  }

  public static List<Paddle> all() {
    return Arrays.stream(paddles).filter(Objects::nonNull).collect(Collectors.toList());
  }

  private Paddle(byte index, float location, World world) {
    this.index = index;
    this.location = location;
    this.world = world;

    body = createBody();
    updateBodyTransform();
  }

  public PaddleState getState() {
    return new PaddleState(index, location);
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
    paddles[index] = null;
    world.destroyBody(body);
  }
}
