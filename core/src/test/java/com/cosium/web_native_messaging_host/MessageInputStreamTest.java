package com.cosium.web_native_messaging_host;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
class MessageInputStreamTest {

  @Test
  @DisplayName("available()")
  void test1() throws IOException {
    byte[] content = "helloworld".getBytes(StandardCharsets.UTF_8);
    assertThat(new MessageInputStream(createStdin(content), 5).available()).isEqualTo(5);
    assertThat(new MessageInputStream(createStdin(content), 10).available()).isEqualTo(10);
    assertThat(new MessageInputStream(createStdin(content), 0).available()).isZero();
  }

  @Test
  @DisplayName("available() after read()")
  void test2() throws IOException {
    MessageInputStream messageInputStream =
        new MessageInputStream(createStdin("helloworld".getBytes(StandardCharsets.UTF_8)), 5);
    messageInputStream.read();
    assertThat(messageInputStream.available()).isEqualTo(4);
  }

  @Test
  @DisplayName("read()")
  void test3() throws IOException {
    byte[] content = "helloworld".getBytes(StandardCharsets.UTF_8);

    assertThat(IOUtils.toString(new MessageInputStream(createStdin(content), 10)))
        .isEqualTo("helloworld");
    assertThat(IOUtils.toString(new MessageInputStream(createStdin(content), 5)))
        .isEqualTo("hello");
    assertThat(IOUtils.toString(new MessageInputStream(createStdin(content), 0))).isEmpty();
  }

  @Test
  @DisplayName("read(byte[] buffer, int offset, int len)")
  void test4() throws IOException {
    MessageInputStream messageInputStream =
        new MessageInputStream(createStdin("helloworld".getBytes(StandardCharsets.UTF_8)), 9);
    byte[] buffer = new byte[9];
    messageInputStream.read(buffer, 0, 5);
    messageInputStream.read(buffer, 5, 4);
    assertThat(messageInputStream.read()).isEqualTo(-1);
    assertThat(new String(buffer, StandardCharsets.UTF_8)).isEqualTo("helloworl");
  }

  private StdinLease createStdin(byte[] content) {
    return new MyStdinLease(new ByteArrayInputStream(content));
  }

  private record MyStdinLease(InputStream inputStream) implements StdinLease {

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
  }
}
