package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PaddleState implements Transferable<PaddleState> {
  static final PaddleState EXAMPLE = new PaddleState();
  public byte index;
  public float location;

  PaddleState() {
  }

  public PaddleState(byte index, float location) {
    this.index = index;
    this.location = location;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeFloat(location);
  }

  @Override
  public PaddleState deserialize(Deserializer deserializer) throws SerializationException {
    return new PaddleState(deserializer.deserializeByte(), deserializer.deserializeFloat());
  }
}
