package com.cosium.web_native_messaging_host;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Réda Housni Alaoui
 */
public interface StdinLease {

  /**
   * @see InputStream#available()
   */
  int available() throws IOException;

  /**
   * @see InputStream#read()
   */
  int read() throws IOException;

  /**
   * @see InputStream#read(byte[], int, int)
   */
  int read(byte[] buffer, int off, int len) throws IOException;

  /**
   * @see java.io.InputStream#readNBytes(int)
   */
  byte[] readNBytes(int len) throws IOException;
}
