package com.cosium.web_native_messaging_host;

import com.fasterxml.jackson.databind.node.ContainerNode;

/**
 * @author Réda Housni Alaoui
 */
public interface MessageHandler {

  void onMessage(Channel channel, ContainerNode<?> message);
}
