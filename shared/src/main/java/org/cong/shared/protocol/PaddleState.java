package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PaddleState implements Transferable<PaddleState> {
  static final PaddleState EXAMPLE = new PaddleState();
  public byte index;
  public float location;
  public boolean sizeIncreased;

  PaddleState() {
  }

  public PaddleState(byte index, float location, boolean sizeIncreased) {
    this.index = index;
    this.location = location;
    this.sizeIncreased = sizeIncreased;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeFloat(location).serializeBoolean(sizeIncreased);
  }

  @Override
  public PaddleState deserialize(Deserializer deserializer) throws SerializationException {
    return new PaddleState(deserializer.deserializeByte(), deserializer.deserializeFloat(), deserializer.deserializeBoolean());
  }
}
