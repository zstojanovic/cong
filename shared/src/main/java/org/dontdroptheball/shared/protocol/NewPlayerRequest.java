package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class NewPlayerRequest implements Transferable<NewPlayerRequest> {
  static final NewPlayerRequest EXAMPLE = new NewPlayerRequest();
  public String name;

  NewPlayerRequest() {
  }

  public NewPlayerRequest(String name) {
    this.name = name;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeString(name);
  }

  @Override
  public NewPlayerRequest deserialize(Deserializer deserializer) throws SerializationException {
    return new NewPlayerRequest(deserializer.deserializeString());
  }
}
