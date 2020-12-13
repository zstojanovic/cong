package org.dontdroptheball.server;

import org.dontdroptheball.shared.Const;

import java.util.Optional;

public class Player extends Identifiable {
  static Repository<Player> repo = new Repository<>(new Player[Const.MAX_PLAYERS]);

  String name;
  Optional<Paddle> paddle;

  private Player(byte id, String name, Optional<Paddle> paddle) {
    super(id);
    this.name = name;
    this.paddle = paddle;
  }

  static Optional<Player> create(String name, Optional<Paddle> paddle) {
    return repo.create(id -> new Player(id, name, paddle));
  }

  void dispose() {
    paddle.ifPresent(Paddle::dispose);
    repo.remove(this);
  }
}
