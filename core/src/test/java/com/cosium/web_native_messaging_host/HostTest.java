package com.cosium.web_native_messaging_host;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.jr.ob.JSON;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
class HostTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MyMessageHandler messageHandler;
  private MyStdin stdin;
  private MyStdout stdout;

  private Host host;

  @BeforeEach
  void beforeEach() {
    messageHandler = new MyMessageHandler();
    stdin = new MyStdin();
    stdout = new MyStdout();
    host = Host.builder(messageHandler).stdin(stdin).stdout(stdout).build();
  }

  @AfterEach
  void afterEach() {
    host.shutdown();
  }

  @Test
  @DisplayName("It provides a single open channel")
  void test1() {
    assertThat(host.openChannel()).isSameAs(host.openChannel());
  }

  @Test
  @DisplayName("It does not provide a closed channel")
  void test2() {
    CloseableChannel firstChannel = host.openChannel();
    firstChannel.close();
    assertThat(host.openChannel()).isNotSameAs(firstChannel);
  }

  @Test
  @DisplayName("Opening a channel starts StdIn and StdOut leases")
  void test3() {
    assertThat(stdin.inProgressLease).isFalse();
    assertThat(stdout.inProgressLease).isFalse();

    host.openChannel();

    assertThat(stdin.inProgressLease).isTrue();
    assertThat(stdout.inProgressLease).isTrue();
  }

  @Test
  @DisplayName("Closing a channel closes StdIn and StdOut leases")
  void test4() {
    CloseableChannel channel = host.openChannel();
    assertThat(stdin.inProgressLease).isTrue();
    assertThat(stdout.inProgressLease).isTrue();
    channel.close();
    assertThat(stdin.inProgressLease).isFalse();
    assertThat(stdout.inProgressLease).isFalse();
  }

  @Test
  @DisplayName("Messages read from stdin are sent to the MessageHandler")
  void test5() throws IOException, InterruptedException {
    byte[] frame =
        createFrame(JSON.std.composeBytes().startObject().put("name", "foo").end().finish());
    stdin.inputStream = new ByteArrayInputStream(frame);
    host.openChannel();
    ContainerNode<?> receivedMessage = messageHandler.messages.poll(1, TimeUnit.MINUTES);
    assertThat(receivedMessage).isNotNull();
    assertThat(receivedMessage.get("name").asText()).isEqualTo("foo");
  }

  @Test
  @DisplayName("Non json messages read from stdin are skipped")
  void test6() throws InterruptedException, IOException {
    byte[] incorrectFrame = createFrame("hello".getBytes(StandardCharsets.UTF_8));
    byte[] correctFrame =
        createFrame(JSON.std.composeBytes().startObject().put("name", "foo").end().finish());
    stdin.inputStream = new ByteArrayInputStream(ArrayUtils.addAll(incorrectFrame, correctFrame));
    host.openChannel();
    ContainerNode<?> receivedMessage = messageHandler.messages.poll(1, TimeUnit.MINUTES);
    assertThat(receivedMessage).isNotNull();
    assertThat(receivedMessage.get("name").asText()).isEqualTo("foo");
  }

  @Test
  @DisplayName("Channel send messages to stdout")
  void test7() throws IOException, InterruptedException {
    ContainerNode<?> message =
        (ContainerNode<?>)
            objectMapper.readTree(
                JSON.std.composeBytes().startObject().put("name", "foo").end().finish());
    host.openChannel().sendMessage(message);
    byte[] output = stdout.byteArrayOutputStream.toByteArray();
    ContainerNode<?> sentMessage = new Messages().read(new MyStdin(output));
    assertThat(sentMessage.get("name").asText()).isEqualTo("foo");
  }

  private byte[] createFrame(byte[] message) {
    byte[] messageLength = UInt32.fromInt(message.length).toByteArray();
    return ArrayUtils.addAll(messageLength, message);
  }

  private static class MyMessageHandler implements MessageHandler {

    private final BlockingQueue<ContainerNode<?>> messages = new LinkedBlockingQueue<>();

    @Override
    public void onMessage(Channel channel, ContainerNode<?> message) {
      messages.add(message);
    }
  }

  private static class MyStdin implements Stdin, CloseableStdinLease {

    private boolean inProgressLease;
    private InputStream inputStream;

    public MyStdin() {
      this(
          new InputStream() {
            @Override
            public int read() {
              return -1;
            }
          });
    }

    public MyStdin(byte[] byteArray) {
      this(new ByteArrayInputStream(byteArray));
    }

    public MyStdin(InputStream inputStream) {
      this.inputStream = inputStream;
    }

    @Override
    public CloseableStdinLease startLease() {
      inProgressLease = true;
      return this;
    }

    @Override
    public int available() throws IOException {
      return inputStream.available();
    }

    @Override
    public int read() throws IOException {
      return inputStream.read();
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
      return inputStream.read(buffer, off, len);
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
      return inputStream.readNBytes(len);
    }

    @Override
    public void close() {
      inProgressLease = false;
    }
  }

  private static class MyStdout implements Stdout, CloseableStdoutLease {

    private boolean inProgressLease;
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Override
    public CloseableStdoutLease startLease() {
      inProgressLease = true;
      return this;
    }

    @Override
    public void write(byte[] buf) throws IOException {
      byteArrayOutputStream.write(buf);
    }

    @Override
    public void close() {
      inProgressLease = false;
    }
  }
}
