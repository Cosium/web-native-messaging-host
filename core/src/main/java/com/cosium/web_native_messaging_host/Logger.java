package com.cosium.web_native_messaging_host;

/**
 * @author Réda Housni Alaoui
 */
public interface Logger {

  /**
   * @param message Can be null
   * @param throwable Can be null
   */
  void debug(String message, Throwable throwable);

  /**
   * @param message Can be null
   * @param throwable Can be null
   */
  void warn(String message, Throwable throwable);

  /**
   * @param message Can be null
   * @param throwable Can be null
   */
  void error(String message, Throwable throwable);
}
