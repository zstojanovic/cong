package org.dontdroptheball.shared.protocol;

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

  GameState() {
  }

  public GameState(
    float playTimer, float record, BallState[] ballStates, PaddleState[] paddleStates, PowerUpState[] powerUpStates
  ) {
    this.playTimer = playTimer;
    this.record = record;
    this.ballStates = ballStates;
    this.paddleStates = paddleStates;
    this.powerUpStates = powerUpStates;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer
      .serializeFloat(playTimer).serializeFloat(record).serializeTransferableArray(ballStates)
      .serializeTransferableArray(paddleStates).serializeTransferableArray(powerUpStates);
  }

  @Override
  public GameState deserialize(Deserializer deserializer) throws SerializationException {
    return new GameState(
      deserializer.deserializeFloat(),
      deserializer.deserializeFloat(),
      deserializer.deserializeTransferableArray(BallState.EXAMPLE, BallState[]::new),
      deserializer.deserializeTransferableArray(PaddleState.EXAMPLE, PaddleState[]::new),
      deserializer.deserializeTransferableArray(PowerUpState.EXAMPLE, PowerUpState[]::new));
  }
}
