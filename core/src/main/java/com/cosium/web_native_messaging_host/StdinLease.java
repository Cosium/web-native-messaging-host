package com.cosium.web_native_messaging_host;

import java.io.IOException;

/**
 * @author Réda Housni Alaoui
 */
public interface StdinLease {

  int read() throws IOException;

  int read(byte[] buffer) throws IOException;
}
