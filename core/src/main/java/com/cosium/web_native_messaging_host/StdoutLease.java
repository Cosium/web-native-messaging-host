package com.cosium.web_native_messaging_host;

import java.io.IOException;

/**
 * @author Réda Housni Alaoui
 */
public interface StdoutLease {
  void write(byte[] buf) throws IOException;
}
