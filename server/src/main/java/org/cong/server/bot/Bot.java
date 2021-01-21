package org.cong.server.bot;

import org.cong.server.Paddle;

public abstract class Bot {
  protected String name;
  protected Paddle paddle;

  public void setup(Paddle paddle) {
    this.paddle = paddle;
  }

  public String name() {
    return name;
  }

  public abstract void think(float delta);
}