package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class PowerUpState implements Transferable<PowerUpState> {
  public enum Type {BALL_FREEZE, EXTRA_BALL, PADDLE_SLOWDOWN, PADDLE_GROWTH }

  static final PowerUpState EXAMPLE = new PowerUpState();
  public byte id;
  public float x;
  public float y;
  public Type type; // TODO maybe extract this to separate message since it's not needed in every PowerUpState

  PowerUpState() {
  }

  public PowerUpState(byte id, float x, float y, Type type) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.type = type;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(id).serializeFloat(x).serializeFloat(y).serializeByte((byte)type.ordinal());
  }

  @Override
  public PowerUpState deserialize(Deserializer deserializer) throws SerializationException {
    return new PowerUpState(
      deserializer.deserializeByte(),
      deserializer.deserializeFloat(),
      deserializer.deserializeFloat(),
      Type.values()[deserializer.deserializeByte()]);
  }
}
