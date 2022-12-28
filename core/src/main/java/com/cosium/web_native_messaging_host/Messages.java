package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Réda Housni Alaoui
 */
class Messages {

  private final ObjectMapper objectMapper;

  public Messages() {
    this(new ObjectMapper());
  }

  public Messages(ObjectMapper objectMapper) {
    this.objectMapper = requireNonNull(objectMapper);
  }

  public ContainerNode<?> read(StdinLease stdin) throws IOException {
    byte[] rawLength = new byte[4];
    stdin.read(rawLength);
    int messageLength = new UInt32(rawLength).toInt();

    MessageInputStream messageInputStream = new MessageInputStream(stdin, messageLength);
    try {
      JsonNode jsonNode = objectMapper.readTree(messageInputStream);
      if (!(jsonNode instanceof ContainerNode<?> containerNode)) {
        throw new IllegalArgumentException(
            "%s is not a %s".formatted(jsonNode, ContainerNode.class));
      }
      return containerNode;
    } finally {
      consumeAndClose(messageInputStream);
    }
  }

  public void write(ContainerNode<?> message, StdoutLease stdout) throws IOException {
    byte[] messageAsBytes = objectMapper.writeValueAsBytes(message);
    int messageLength = messageAsBytes.length;

    stdout.write(UInt32.fromInt(messageLength).toByteArray());
    stdout.write(messageAsBytes);
  }

  private void consumeAndClose(InputStream inputStream) throws IOException {
    try (inputStream) {
      inputStream.readAllBytes();
    }
  }
}
