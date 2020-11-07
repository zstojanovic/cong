package org.dontdroptheball.shared;

import com.github.czyzby.websocket.serialization.impl.ManualSerializer;

public class StateSerializer extends ManualSerializer {

  public StateSerializer() {
    super();
    register(KeyEvent.EXAMPLE);
    register(GameState.EXAMPLE);
    register(BallState.EXAMPLE);
    register(PlayerState.EXAMPLE);
  }
}
