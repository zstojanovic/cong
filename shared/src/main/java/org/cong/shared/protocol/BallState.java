package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class BallState implements Transferable<BallState> {
  static final BallState EXAMPLE = new BallState();
  public byte id;
  public float x;
  public float y;

  BallState() {
  }

  public BallState(byte id, float x, float y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(id).serializeFloat(x).serializeFloat(y);
  }

  @Override
  public BallState deserialize(Deserializer deserializer) throws SerializationException {
    return new BallState(
      deserializer.deserializeByte(),
      deserializer.deserializeFloat(),
      deserializer.deserializeFloat());
  }
}
