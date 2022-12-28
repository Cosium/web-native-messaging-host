package com.cosium.web_native_messaging_host;

import com.fasterxml.jackson.databind.node.ContainerNode;

/**
 * @author Réda Housni Alaoui
 */
public interface Channel {

  /** Sends a message to the other side */
  void sendMessage(ContainerNode<?> message);

  /** Pauses the current Thread until the Channel shutdowns */
  void waitForShutdown();
}
