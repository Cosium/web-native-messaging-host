package com.cosium.web_native_messaging_host;

/**
 * @author Réda Housni Alaoui
 */
public interface CloseableStdinLease extends StdinLease, AutoCloseable {

  @Override
  void close();
}
