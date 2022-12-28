package com.cosium.web_native_messaging_host_test_app;

import com.cosium.web_native_messaging_host.Channel;
import com.cosium.web_native_messaging_host.CloseableChannel;
import com.cosium.web_native_messaging_host.Host;
import com.cosium.web_native_messaging_host.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import java.util.Map;

/**
 * @author Réda Housni Alaoui
 */
public class App implements MessageHandler {

  public static void main(String[] args) {
    ObjectMapper objectMapper = new ObjectMapper();
    try (CloseableChannel channel = Host.builder(new App()).build().openChannel()) {
      channel.sendMessage(objectMapper.valueToTree(Map.of("type", "heartbeat")));
      channel.waitForShutdown();
    }
  }

  @Override
  public void onMessage(Channel channel, ContainerNode<?> message) {
    channel.sendMessage(message);
  }
}
