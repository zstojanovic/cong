package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class NewPlayerAnnouncement implements Transferable<NewPlayerAnnouncement> {
  static final NewPlayerAnnouncement EXAMPLE = new NewPlayerAnnouncement();
  public byte index;
  public String name;

  NewPlayerAnnouncement() {
  }

  public NewPlayerAnnouncement(byte index, String name) {
    this.index = index;
    this.name = name;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeString(name);
  }

  @Override
  public NewPlayerAnnouncement deserialize(Deserializer deserializer) throws SerializationException {
    return new NewPlayerAnnouncement(deserializer.deserializeByte(), deserializer.deserializeString());
  }
}
