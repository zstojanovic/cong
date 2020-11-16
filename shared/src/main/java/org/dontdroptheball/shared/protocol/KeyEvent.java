package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class KeyEvent implements Transferable<KeyEvent> {

  public enum Code {
    LEFT_PRESSED, LEFT_RELEASED, RIGHT_PRESSED, RIGHT_RELEASED
  }

  static final KeyEvent EXAMPLE = new KeyEvent();
  public Code code;

  KeyEvent() {
  }

  public KeyEvent(Code code) {
    this.code = code;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte((byte)code.ordinal());
  }

  @Override
  public KeyEvent deserialize(Deserializer deserializer) throws SerializationException {
    return new KeyEvent(Code.values()[deserializer.deserializeByte()]);
  }
}
