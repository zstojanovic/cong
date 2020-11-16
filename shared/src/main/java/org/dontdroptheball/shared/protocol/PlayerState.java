package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PlayerState implements Transferable<PlayerState> {
  static final PlayerState EXAMPLE = new PlayerState();
  public byte index;
  public String name; // TODO consider moving this to separate "new player" messages to keep packet size low
  public float location;

  PlayerState() {
  }

  public PlayerState(byte index, String name, float location) {
    this.index = index;
    this.name = name;
    this.location = location;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeString(name).serializeFloat(location);
  }

  @Override
  public PlayerState deserialize(Deserializer deserializer) throws SerializationException {
    return new PlayerState(
      deserializer.deserializeByte(),
      deserializer.deserializeString(),
      deserializer.deserializeFloat());
  }
}
