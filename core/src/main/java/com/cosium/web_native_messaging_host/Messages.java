package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import java.io.IOException;

/**
 * @author Réda Housni Alaoui
 */
class Messages {

  private static final int MESSAGE_SIZE_LENGTH = 4;

  private final ObjectMapper objectMapper;

  public Messages() {
    this(new ObjectMapper());
  }

  public Messages(ObjectMapper objectMapper) {
    this.objectMapper = requireNonNull(objectMapper);
  }

  public ContainerNode<?> read(StdinLease stdin) throws IOException, InterruptedException {
    waitWhile(() -> stdin.available() < MESSAGE_SIZE_LENGTH);
    byte[] rawLength = stdin.readNBytes(MESSAGE_SIZE_LENGTH);
    if (rawLength.length < MESSAGE_SIZE_LENGTH) {
      throw new IOException("End of stream reached");
    }

    int messageLength = new UInt32(rawLength).toInt();
    waitWhile(() -> stdin.available() < messageLength);

    try (MessageInputStream messageInputStream = new MessageInputStream(stdin, messageLength)) {
      JsonNode jsonNode = objectMapper.readTree(messageInputStream);
      if (!(jsonNode instanceof ContainerNode<?> message)) {
        throw new IOException("%s is not a %s".formatted(jsonNode, ContainerNode.class));
      }
      return message;
    }
  }

  public void write(ContainerNode<?> message, StdoutLease stdout) throws IOException {
    byte[] messageAsBytes = objectMapper.writeValueAsBytes(message);
    int messageLength = messageAsBytes.length;

    stdout.write(UInt32.fromInt(messageLength).toByteArray());
    stdout.write(messageAsBytes);
  }

  private void waitWhile(IOCondition condition) throws IOException, InterruptedException {
    while (condition.isTruthy()) {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException();
      }
      Thread.sleep(100);
    }
  }

  private interface IOCondition {
    boolean isTruthy() throws IOException;
  }
}
