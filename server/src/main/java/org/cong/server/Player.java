package org.cong.server;

import org.cong.server.bot.Bot;
import org.cong.shared.Const;

import java.util.Optional;

public class Player extends Identifiable {
  static Repository<Player> repo = new Repository<>(new Player[Const.MAX_PLAYERS]);

  final String name;
  private Optional<Paddle> paddle;
  private Optional<Bot> bot;

  private Player(byte id, String name, Optional<Paddle> paddle, Optional<Bot> bot) {
    super(id);
    this.name = name;
    this.paddle = paddle;
    this.bot = bot;
  }

  static Optional<Player> create(String name, Optional<Paddle> paddle, Optional<Bot> bot) {
    return repo.create(id -> new Player(id, name, paddle, bot));
  }

  void step(float delta) {
    bot.ifPresent(b -> b.think(delta));
  }

  void setPaddle(Optional<Paddle> paddle) {
    this.paddle = paddle;
  }

  boolean dropPaddle() {
    if (paddle.isPresent()) {
      paddle.get().dispose();
      paddle = Optional.empty();
      return true;
    } else {
      return false;
    }
  }

  Optional<Paddle> paddle() {
    return paddle;
  }

  Optional<Bot> bot() {
    return bot;
  }

  @Override
  public String toString() {
    return "Player" + id + " (" + name + ")";
  }

  void dispose() {
    dropPaddle();
    repo.remove(this);
  }
}
