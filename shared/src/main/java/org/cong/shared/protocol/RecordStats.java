package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class RecordStats implements Transferable<RecordStats> {
  static final RecordStats EXAMPLE = new RecordStats();
  public String text;

  RecordStats() {
  }

  public RecordStats(String text) {
    this.text = text;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeString(text);
  }

  @Override
  public RecordStats deserialize(Deserializer deserializer) throws SerializationException {
    return new RecordStats(deserializer.deserializeString());
  }
}
