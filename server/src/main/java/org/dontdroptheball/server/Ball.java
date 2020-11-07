package org.dontdroptheball.server;

import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Arena;
import org.dontdroptheball.shared.BallState;

public class Ball {
  enum Status { COUNTDOWN, PLAY }

  Status status = Status.COUNTDOWN;
  World world;
  float diameter = 0.5f;
  Body body;
  float countdownTimer;
  float playTimer;

  public Ball(World world) {
    this.world = world;
    body = createBody();
    startCountdown();
  }

  public BallState getState() {
    return new BallState(body.getPosition().x, body.getPosition().y);
  }

  private void startCountdown() {
    status = Status.COUNTDOWN;
    countdownTimer = 3;
    body.setTransform(Arena.WIDTH/2, Arena.HEIGHT/2, 0);
    body.setLinearVelocity(0, 0);
  }

  private void startPlaying() {
    status = Status.PLAY;
    playTimer = 0;
    body.setLinearVelocity(1.5f, 1.5f);
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
    body.createFixture(fixtureDef);
    shape.dispose();
    return body;
  }

  public void step(float delta) {
    if (status == Status.COUNTDOWN) {
      countdownTimer -= delta;
      if (countdownTimer <= 0) startPlaying();
    } else {
      var dropped =
        body.getPosition().x < -diameter || body.getPosition().x > (Arena.WIDTH + diameter) ||
        body.getPosition().y < -diameter || body.getPosition().y > (Arena.HEIGHT + diameter);
      if (dropped) {
        startCountdown();
      } else {
        playTimer += delta;
      }
    }
  }
}
