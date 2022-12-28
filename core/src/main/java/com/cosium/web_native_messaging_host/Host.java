package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Réda Housni Alaoui
 */
public class Host {

  private final Lock lock = new ReentrantLock();

  private final ObjectMapper objectMapper;
  private final MessageHandler messageHandler;
  private final Logger logger;
  private final Stdin stdin;
  private final Stdout stdout;
  private DefaultChannel openChannel;

  private Host(Builder builder) {
    objectMapper = new ObjectMapper();
    messageHandler = builder.messageHandler;
    logger = builder.logger;
    stdin = builder.stdin;
    stdout = builder.stdout;
  }

  public static Builder builder(MessageHandler messageHandler) {
    return new Builder(messageHandler);
  }

  /**
   * @return If an open channel already exists, the open channel. Otherwise, a new channel.
   */
  public CloseableChannel openChannel() {
    lock.lock();
    try {
      if (openChannel == null) {
        openChannel =
            DefaultChannel.open(
                objectMapper, this::onChannelShutdown, messageHandler, logger, stdin, stdout);
      }
      return openChannel;
    } finally {
      lock.unlock();
    }
  }

  /** Closes any open channel */
  public void shutdown() {
    lock.lock();
    try {
      if (openChannel == null) {
        return;
      }
      openChannel.close();
    } finally {
      lock.unlock();
    }
  }

  private Runnable onChannelShutdown() {
    lock.lock();
    openChannel = null;
    return lock::unlock;
  }

  public static class Builder {
    private final MessageHandler messageHandler;
    private Logger logger = new SystemErrLogger();
    private Stdin stdin = new SystemStdin();
    private Stdout stdout = new SystemStdout();

    private Builder(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
    }

    public Builder logger(Logger logger) {
      this.logger = requireNonNull(logger);
      return this;
    }

    public Builder stdin(Stdin stdin) {
      this.stdin = requireNonNull(stdin);
      return this;
    }

    public Builder stdout(Stdout stdout) {
      this.stdout = requireNonNull(stdout);
      return this;
    }

    public Host build() {
      return new Host(this);
    }
  }
}
