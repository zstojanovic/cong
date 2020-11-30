package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;
import org.dontdroptheball.shared.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameState implements Transferable<GameState> {
  static final GameState EXAMPLE = new GameState();
  public BallState ballState;
  public List<PaddleState> paddleStates;

  GameState() {
  }

  public GameState(BallState ballState, List<PaddleState> paddleStates) {
    this.ballState = ballState;
    this.paddleStates = paddleStates;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeTransferable(ballState).serializeTransferableArray(paddleStates.toArray(new PaddleState[]{}));
  }

  @Override
  public GameState deserialize(Deserializer deserializer) throws SerializationException {
    var state = new GameState();
    state.ballState = deserializer.deserializeTransferable(BallState.EXAMPLE);
    state.paddleStates = new ArrayList<>();
    var paddleStatesArray = new PaddleState[Const.MAX_PADDLES];
    var length = deserializer.deserializeTransferableArray(paddleStatesArray, PaddleState.EXAMPLE);
    state.paddleStates.addAll(Arrays.asList(paddleStatesArray).subList(0, length));
    return state;
  }
}
