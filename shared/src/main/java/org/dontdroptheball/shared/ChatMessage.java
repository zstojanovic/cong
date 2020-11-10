package org.dontdroptheball.shared;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class ChatMessage implements Transferable<ChatMessage> {
  static final ChatMessage EXAMPLE = new ChatMessage();
  public String timestamp;
  public String playerName;
  public String text;

  ChatMessage() {
  }

  public ChatMessage(String timestamp, String playerName, String text) {
    this.timestamp = timestamp;
    this.playerName = playerName;
    this.text = text;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeString(timestamp).serializeString(playerName).serializeString(text);
  }

  @Override
  public ChatMessage deserialize(Deserializer deserializer) throws SerializationException {
    return new ChatMessage(
      deserializer.deserializeString(),
      deserializer.deserializeString(),
      deserializer.deserializeString());
  }
}
