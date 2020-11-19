package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.impl.ManualSerializer;

public class ProtocolSerializer extends ManualSerializer {

  public ProtocolSerializer() {
    super();
    register(KeyEvent.EXAMPLE);
    register(GameState.EXAMPLE);
    register(BallState.EXAMPLE);
    register(PlayerState.EXAMPLE);
    register(ChatMessage.EXAMPLE);
    register(NewPlayerRequest.EXAMPLE);
    register(NewPlayerResponse.EXAMPLE);
    register(NewPlayerAnnouncement.EXAMPLE);
  }
}
