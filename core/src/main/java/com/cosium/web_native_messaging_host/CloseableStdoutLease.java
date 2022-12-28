package com.cosium.web_native_messaging_host;

/**
 * @author Réda Housni Alaoui
 */
public interface CloseableStdoutLease extends StdoutLease, AutoCloseable {

  @Override
  void close();
}
