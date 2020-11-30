package org.dontdroptheball.server;

import org.dontdroptheball.shared.Const;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Player {
  byte id;
  String name;
  Optional<Paddle> paddle;

  private static Player[] players = new Player[Const.MAX_PLAYERS];

  private Player(byte id, String name, Optional<Paddle> paddle) {
    this.id = id;
    this.name = name;
    this.paddle = paddle;
  }

  public static Player create(String name, Optional<Paddle> paddle) {
    byte newId = 0;
    while (newId < Const.MAX_PLAYERS && players[newId] != null) newId++;
    if (newId == Const.MAX_PLAYERS) throw new RuntimeException("Too many players");
    players[newId] = new Player(newId, name.substring(0, Math.min(name.length(), 10)), paddle);
    return players[newId];
  }

  public static List<Player> all() {
    return Arrays.stream(players).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public void dispose() {
    players[id] = null;
    paddle.ifPresent(Paddle::dispose);
  }
}
