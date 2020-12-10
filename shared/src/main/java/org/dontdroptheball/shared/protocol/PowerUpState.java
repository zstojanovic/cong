package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PowerUpState implements Transferable<PowerUpState> {
  static final PowerUpState EXAMPLE = new PowerUpState();
  public byte id;
  public float x;
  public float y;

  PowerUpState() {
  }

  public PowerUpState(byte id, float x, float y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(id).serializeFloat(x).serializeFloat(y);
  }

  @Override
  public PowerUpState deserialize(Deserializer deserializer) throws SerializationException {
    return new PowerUpState(
      deserializer.deserializeByte(),
      deserializer.deserializeFloat(),
      deserializer.deserializeFloat());
  }
}
