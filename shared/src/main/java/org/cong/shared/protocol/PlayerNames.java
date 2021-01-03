package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PlayerNames implements Transferable<PlayerNames> {
  static final PlayerNames EXAMPLE = new PlayerNames();
  public String[] names;

  PlayerNames() {
  }

  public PlayerNames(String[] names) {
    this.names = names;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeStringArray(names);
  }

  @Override
  public PlayerNames deserialize(Deserializer deserializer) throws SerializationException {
    return new PlayerNames(deserializer.deserializeStringArray());
  }
}
