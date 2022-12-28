package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Réda Housni Alaoui
 */
class FailingInputStream extends InputStream {

  private final String exceptionMessage;

  FailingInputStream(String exceptionMessage) {
    this.exceptionMessage = requireNonNull(exceptionMessage);
  }

  @Override
  public int read() {
    throw new UnsupportedOperationException(exceptionMessage);
  }

  @Override
  public int available() {
    throw new UnsupportedOperationException(exceptionMessage);
  }

  @Override
  public synchronized void mark(int readLimit) {
    throw new UnsupportedOperationException(exceptionMessage);
  }

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException(exceptionMessage);
  }
}
