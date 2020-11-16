package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class NewPlayerResponse implements Transferable<NewPlayerResponse> {
  static final NewPlayerResponse EXAMPLE = new NewPlayerResponse();
  public byte index;

  NewPlayerResponse() {
  }

  public NewPlayerResponse(byte index) {
    this.index = index;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index);
  }

  @Override
  public NewPlayerResponse deserialize(Deserializer deserializer) throws SerializationException {
    return new NewPlayerResponse(deserializer.deserializeByte());
  }
}
