package com.cosium.web_native_messaging_host;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Réda Housni Alaoui
 */
class MessageInputStream extends InputStream {

  private final StdinLease stdin;
  private final int messageLength;
  private int cursorPosition;

  MessageInputStream(StdinLease stdin, int messageLength) {
    this.stdin = stdin;
    this.messageLength = messageLength;
  }

  @Override
  public int available() throws IOException {
    if (cursorPosition >= messageLength) {
      return 0;
    }
    return Math.min(stdin.available(), messageLength - cursorPosition);
  }

  @Override
  public int read() throws IOException {
    if (cursorPosition >= messageLength) {
      return -1;
    }
    int byteRead = stdin.read();
    cursorPosition++;
    return byteRead;
  }

  @Override
  public int read(byte[] buffer, int offset, int len) throws IOException {
    if (cursorPosition >= messageLength) {
      return -1;
    }
    int byteRead = stdin.read(buffer, offset, Math.min(len, messageLength - cursorPosition));
    cursorPosition += byteRead;
    return byteRead;
  }

  @Override
  public void close() throws IOException {
    readAllBytes();
  }
}
