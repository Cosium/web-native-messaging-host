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
  public int read() throws IOException {
    if (cursorPosition >= messageLength) {
      return -1;
    }
    int byteRead = stdin.read();
    cursorPosition++;
    return byteRead;
  }
}
