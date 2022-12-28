package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import java.io.OutputStream;

/**
 * @author Réda Housni Alaoui
 */
class FailingOutputStream extends OutputStream {

  private final String exceptionMessage;

  FailingOutputStream(String exceptionMessage) {
    this.exceptionMessage = requireNonNull(exceptionMessage);
  }

  @Override
  public void write(int b) {
    throw new UnsupportedOperationException(exceptionMessage);
  }

  @Override
  public void flush() {
    throw new UnsupportedOperationException(exceptionMessage);
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException(exceptionMessage);
  }
}
