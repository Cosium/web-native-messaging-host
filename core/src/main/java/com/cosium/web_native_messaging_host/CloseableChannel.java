package com.cosium.web_native_messaging_host;

/**
 * @author Réda Housni Alaoui
 */
public interface CloseableChannel extends Channel, AutoCloseable {
  @Override
  void close();
}
