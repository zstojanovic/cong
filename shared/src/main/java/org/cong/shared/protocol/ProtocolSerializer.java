package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.impl.ManualSerializer;

public class ProtocolSerializer extends ManualSerializer {

  public ProtocolSerializer() {
    super();
    register(KeyEvent.EXAMPLE);
    register(GameState.EXAMPLE);
    register(BallState.EXAMPLE);
    register(PaddleState.EXAMPLE);
    register(ChatMessage.EXAMPLE);
    register(RecordStats.EXAMPLE);
    register(PowerUpState.EXAMPLE);
    register(NewPlayerRequest.EXAMPLE);
    register(NewPlayerResponse.EXAMPLE);
    register(RecordStatsRequest.EXAMPLE);
  }
}
