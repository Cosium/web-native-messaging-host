[![Build Status](https://github.com/Cosium/web-native-messaging-host/actions/workflows/ci.yml/badge.svg)](https://github.com/Cosium/web-native-messaging-host/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.cosium.web_native_messaging_host/web-native-messaging-host.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.cosium.web_native_messaging_host%22%20AND%20a%3A%22web-native-messaging-host%22)

# Web Native Messaging Host

A java library allowing to turn any JVM application into a [Web Native Messaging Host](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Native_messaging) .

# Gotchas

To date, [web native messaging protocol](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Native_messaging) only supports `stdio` communication:
- messages are sent to the native application's `stdin`  
- messages are sent to the browser extension via native application's `stdout`

Native application (e.g. your JVM application) instances started by the browser must never use `System.out` for anything else than native messaging. Very often, you will have to make sure your logger never writes to `System.out`.

As soon as a `Channel` is open, the current library will swap `System.in` and `System.out` for instances throwing exception on any interaction attempt coming from components foreign to the current library. On `Channel` shutdown, the library will swap back the standard `System.in` and `System.out` instances. The current library cannot cover the full application lifecycle, therefore it is only a best effort.

Using `System.out` (e.g. for logging) before opening a `Channel` will trigger a fatal error on the browser side leading the end of the communication channel and the native application shutdown.

# Quick start

1. Add the following dependency:
    ```xml
    <dependency>
      <groupId>com.cosium.web_native_messaging_host</groupId>
      <artifactId>web-native-messaging-host</artifactId>
      <version>${web-native-messaging-host.version}</version>
    </dependency>
    ```
2. Open the communication channel:
    ```java
    public class App implements MessageHandler {
   
      private final ObjectMapper objectMapper = new ObjectMapper();

      public static void main(String[] args) {
        Host host = Host.builder(new App()).build();
        // Open the communication channel
        try (CloseableChannel channel = host.openChannel()) {
          // Block until channel shutdown
          channel.waitForShutdown();
        }
      }
    
      @Override
      public void onMessage(Channel channel, ContainerNode<?> rawMessage) {
        Message message = objectMapper.convertValue(rawMessage, Message.class);
        // Print the received message
        System.out.printf("Received message '%s'%n", message.body);
        // Send a message to the other end
        channel.sendMessage(objectMapper.valueToTree(new Message("I got your message")));
      }
   
      private record Message(String body) {
      } 
    }
    ```

# Build

`e2e-tests` can only run on Unix systems.

```shell
./mvnw clean verify
```

# Release

```shell
./release.sh
```
