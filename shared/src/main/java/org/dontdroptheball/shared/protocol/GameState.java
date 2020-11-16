package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;
import org.dontdroptheball.shared.Arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameState implements Transferable<GameState> {
  static final GameState EXAMPLE = new GameState();
  public BallState ballState;
  public List<PlayerState> playerStates;

  GameState() {
  }

  public GameState(BallState ballState, List<PlayerState> playerStates) {
    this.ballState = ballState;
    this.playerStates = playerStates;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeTransferable(ballState).serializeTransferableArray(playerStates.toArray(new PlayerState[]{}));
  }

  @Override
  public GameState deserialize(Deserializer deserializer) throws SerializationException {
    var state = new GameState();
    state.ballState = deserializer.deserializeTransferable(BallState.EXAMPLE);
    state.playerStates = new ArrayList<>();
    var playerStatesArray = new PlayerState[Arena.MAX_PLAYERS];
    var length = deserializer.deserializeTransferableArray(playerStatesArray, PlayerState.EXAMPLE);
    state.playerStates.addAll(Arrays.asList(playerStatesArray).subList(0, length));
    return state;
  }
}
