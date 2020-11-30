package org.dontdroptheball.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import org.dontdroptheball.shared.Const;
import org.dontdroptheball.shared.protocol.BallState;

public class Ball {
  enum Status { COUNTDOWN, PLAY }

  Preferences preferences;
  Status status = Status.COUNTDOWN;
  GameServer server;
  float diameter = 0.5f;
  Body body;
  float countdownTimer;
  float playTimer;
  float record;

  public Ball(GameServer server) {
    this.server = server;
    preferences = Gdx.app.getPreferences("dontdroptheball-server");
    record = preferences.getFloat("record");
    body = createBody();
    startCountdown();
  }

  public BallState getState() {
    return new BallState(body.getPosition().x, body.getPosition().y, playTimer, record);
  }

  private void startCountdown() {
    status = Status.COUNTDOWN;
    countdownTimer = 3;
    if (playTimer > record) {
      record = playTimer;
      preferences.putFloat("record", record);
      preferences.flush();
    }
    body.setTransform(Const.WIDTH/2, Const.HEIGHT/2, 0);
    body.setLinearVelocity(0, 0);
  }

  private void startPlaying() {
    status = Status.PLAY;
    playTimer = 0;
    var paddle = server.getRandomPaddle();
    float direction = paddle
      .map(p -> p.body.getPosition().sub(body.getPosition()).angleRad() + MathUtils.random(-0.04f, 0.04f))
      .orElseGet(() -> MathUtils.random() * MathUtils.PI2);
    body.setLinearVelocity(MathUtils.cos(direction) * 2.5f, MathUtils.sin(direction) * 2.5f);
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
        body.getPosition().x < -diameter || body.getPosition().x > (Const.WIDTH + diameter) ||
        body.getPosition().y < -diameter || body.getPosition().y > (Const.HEIGHT + diameter);
      if (dropped) {
        startCountdown();
      } else {
        playTimer += delta;
      }
    }
  }
}
