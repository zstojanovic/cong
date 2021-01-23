package org.cong.server.bot;

import com.badlogic.gdx.math.MathUtils;
import org.cong.server.Ball;
import org.cong.shared.Const;

public class IffyBot extends Bot {
  private static int nextId = 0;
  private final int id;
  private Ball ball;

  public IffyBot() {
    name = "Iffy_" + MathUtils.random(999);
    id = nextId;
    nextId++;
  }

  @Override
  public void think(float delta) {
    ball = Ball.repo.first();
    if ((id / 4) % 2 == 1) {
      ball = Ball.repo.stream().skip(1).findFirst().orElseGet(() -> ball);
    }

    var ballVelocity = ball.linearVelocity();
    var ballPos = ball.position();
    var paddlePos = paddle.position();
    var dx = ballPos.x - paddlePos.x;
    var dy = ballPos.y - paddlePos.y;

    if (paddle.location() < Const.Path.POINT1) {
      if (id % 4 != 0) {
        paddle.goLeft();
      } else if (dx > 0.4 && ballVelocity.y < 0) {
        paddle.goLeft();
      } else if (dx < -0.4 && ballVelocity.y < 0) {
        paddle.goRight();
      } else {
        paddle.stop();
      }
    } else if (paddle.location() >= Const.Path.POINT1 && paddle.location() < Const.Path.POINT2) {
      if (id % 4 != 1) {
        paddle.goLeft();
      } else if (dy > 0.4 && ballVelocity.x > 0) {
        paddle.goLeft();
      } else if (dy < -0.4 && ballVelocity.x > 0) {
        paddle.goRight();
      } else {
        paddle.stop();
      }
    } else if (paddle.location() >= Const.Path.POINT2 && paddle.location() < Const.Path.POINT3) {
      if (id % 4 != 2) {
        paddle.goLeft();
      } else if (dx > 0.4 && ballVelocity.y > 0) {
        paddle.goRight();
      } else if (dx < -0.4 && ballVelocity.y > 0) {
        paddle.goLeft();
      } else {
        paddle.stop();
      }
    } else {
      if (id % 4 != 3) {
        paddle.goLeft();
      } else if (dy > 0.4 && ballVelocity.x < 0) {
        paddle.goRight();
      } else if (dy < -0.4 && ballVelocity.x < 0) {
        paddle.goLeft();
      } else {
        paddle.stop();
      }
    }
  }
}