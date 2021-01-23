package org.cong.server.bot;

import com.badlogic.gdx.math.MathUtils;

public class RandoBot extends Bot {

  public RandoBot() {
    name = "Rando_" + MathUtils.random(999);
  }

  @Override
  public void think(float delta) {
    if (MathUtils.random() < 0.01) {
      switch (MathUtils.random(2)) {
        case 0: paddle.goLeft(); break;
        case 1: paddle.goRight(); break;
        case 2: paddle.stop();
      }
    }
  }
}