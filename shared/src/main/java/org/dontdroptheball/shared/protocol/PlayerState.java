package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PlayerState implements Transferable<PlayerState> {
  static final PlayerState EXAMPLE = new PlayerState();
  public byte index;
  public float location;

  PlayerState() {
  }

  public PlayerState(byte index, float location) {
    this.index = index;
    this.location = location;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeFloat(location);
  }

  @Override
  public PlayerState deserialize(Deserializer deserializer) throws SerializationException {
    return new PlayerState(deserializer.deserializeByte(), deserializer.deserializeFloat());
  }
}
