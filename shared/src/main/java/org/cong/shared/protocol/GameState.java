package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class GameState implements Transferable<GameState> {
  static final GameState EXAMPLE = new GameState();
  public float playTimer;
  public float record;
  public BallState[] ballStates;
  public PaddleState[] paddleStates;
  public PowerUpState[] powerUpStates;
  public boolean bounce;
  public boolean drop;
  public boolean collect;

  private static final byte BOUNCE_MASK = 0x01;
  private static final byte DROP_MASK = 0x02;
  private static final byte COLLECT_MASK = 0x04;

  GameState() {
  }

  public GameState(
    boolean bounce, boolean drop, boolean collect,
    float playTimer, float record, BallState[] ballStates, PaddleState[] paddleStates, PowerUpState[] powerUpStates
  ) {
    this.bounce = bounce;
    this.drop = drop;
    this.collect = collect;
    this.playTimer = playTimer;
    this.record = record;
    this.ballStates = ballStates;
    this.paddleStates = paddleStates;
    this.powerUpStates = powerUpStates;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    var packed = (byte)((bounce ? BOUNCE_MASK : 0) + (drop ? DROP_MASK : 0) + (collect ? COLLECT_MASK : 0));
    serializer
      .serializeByte(packed)
      .serializeFloat(playTimer).serializeFloat(record).serializeTransferableArray(ballStates)
      .serializeTransferableArray(paddleStates).serializeTransferableArray(powerUpStates);
  }

  @Override
  public GameState deserialize(Deserializer deserializer) throws SerializationException {
    var packed = deserializer.deserializeByte();
    return new GameState(
      (packed & BOUNCE_MASK) > 0,
      (packed & DROP_MASK) > 0,
      (packed & COLLECT_MASK) > 0,
      deserializer.deserializeFloat(),
      deserializer.deserializeFloat(),
      deserializer.deserializeTransferableArray(BallState.EXAMPLE, BallState[]::new),
      deserializer.deserializeTransferableArray(PaddleState.EXAMPLE, PaddleState[]::new),
      deserializer.deserializeTransferableArray(PowerUpState.EXAMPLE, PowerUpState[]::new));
  }
}
