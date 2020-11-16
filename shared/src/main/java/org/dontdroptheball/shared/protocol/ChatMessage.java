package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class ChatMessage implements Transferable<ChatMessage> {
  static final ChatMessage EXAMPLE = new ChatMessage();
  public String timestamp;
  public byte playerIndex;
  public String text;

  ChatMessage() {
  }

  public ChatMessage(String timestamp, byte playerIndex, String text) {
    this.timestamp = timestamp;
    this.playerIndex = playerIndex;
    this.text = text;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeString(timestamp).serializeByte(playerIndex).serializeString(text);
  }

  @Override
  public ChatMessage deserialize(Deserializer deserializer) throws SerializationException {
    return new ChatMessage(
      deserializer.deserializeString(),
      deserializer.deserializeByte(),
      deserializer.deserializeString());
  }
}
