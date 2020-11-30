package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;

public class ChatMessage implements Transferable<ChatMessage> {
  static final ChatMessage EXAMPLE = new ChatMessage();
  static final byte SERVER = -1;
  public String timestamp;
  public byte playerId;
  public String text;

  ChatMessage() {
  }

  public ChatMessage(String timestamp, byte playerId, String text) {
    this.timestamp = timestamp;
    this.playerId = playerId;
    this.text = text;
  }

  public ChatMessage(String timestamp, String text) {
    this(timestamp, SERVER, text);
  }

  public boolean isServerMessage() {
    return playerId == SERVER;
  }
  
  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeString(timestamp).serializeByte(playerId).serializeString(text);
  }

  @Override
  public ChatMessage deserialize(Deserializer deserializer) throws SerializationException {
    return new ChatMessage(
      deserializer.deserializeString(),
      deserializer.deserializeByte(),
      deserializer.deserializeString());
  }
}
