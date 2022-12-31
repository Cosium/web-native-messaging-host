package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * @author Réda Housni Alaoui
 */
class DefaultChannel implements CloseableChannel {

  private final Messages messages;
  private final Supplier<Runnable> shutdownHook;
  private final Logger logger;
  private final CloseableStdinLease stdinLease;
  private final CloseableStdoutLease stdoutLease;
  private final Future<?> stdinWatch;
  private boolean closed;

  private DefaultChannel(
      ObjectMapper objectMapper,
      Supplier<Runnable> shutdownHook,
      MessageHandler messageHandler,
      Logger logger,
      Stdin stdin,
      Stdout stdout) {

    this.messages = new Messages(objectMapper);
    this.shutdownHook = requireNonNull(shutdownHook);
    this.logger = requireNonNull(logger);
    this.stdinLease = stdin.startLease();
    this.stdoutLease = stdout.startLease();

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    stdinWatch =
        executorService.submit(new StdinWatch(this, messages, messageHandler, logger, stdinLease));
    executorService.shutdown();
  }

  public static DefaultChannel open(
      ObjectMapper objectMapper,
      Supplier<Runnable> shutdownHook,
      MessageHandler messageHandler,
      Logger logger,
      Stdin stdin,
      Stdout stdout) {

    return new DefaultChannel(objectMapper, shutdownHook, messageHandler, logger, stdin, stdout);
  }

  @Override
  public void sendMessage(ContainerNode<?> message) {
    if (closed) {
      throw new IllegalStateException("This channel is closed");
    }
    try {
      logger.debug("Sending message %s".formatted(message), null);
      messages.write(message, stdoutLease);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void waitForShutdown() {
    try {
      stdinWatch.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException | CancellationException ignored) {
      // Do nothing
    }
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    closed = true;
    Runnable shutdownHookRunnable = null;
    try {
      shutdownHookRunnable = shutdownHook.get();
    } catch (RuntimeException e) {
      logger.error(e.getMessage(), e);
    }

    stdinWatch.cancel(true);
    try {
      stdinWatch.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      logger.warn(e.getMessage(), e);
    } catch (CancellationException ignored) {
      // Do nothing
    }

    stdinLease.close();
    stdoutLease.close();
    Optional.ofNullable(shutdownHookRunnable).ifPresent(Runnable::run);
  }

  private record StdinWatch(
      Channel channel,
      Messages messages,
      MessageHandler messageHandler,
      Logger logger,
      StdinLease stdinLease)
      implements Runnable {

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          ContainerNode<?> message = messages.read(stdinLease);
          logger.debug("Received message %s".formatted(message), null);
          messageHandler.onMessage(channel, message);
        } catch (IOException e) {
          logger.warn(e.getMessage(), e);
        } catch (RuntimeException e) {
          logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
  }
}
