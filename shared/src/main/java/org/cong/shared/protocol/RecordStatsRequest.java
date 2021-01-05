package org.cong.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class RecordStatsRequest implements Transferable<RecordStatsRequest> {
  static final RecordStatsRequest EXAMPLE = new RecordStatsRequest();

  public RecordStatsRequest() {
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
  }

  @Override
  public RecordStatsRequest deserialize(Deserializer deserializer) throws SerializationException {
    return new RecordStatsRequest();
  }
}
