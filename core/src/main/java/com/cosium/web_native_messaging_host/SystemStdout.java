package com.cosium.web_native_messaging_host;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Réda Housni Alaoui
 */
class SystemStdout implements Stdout, CloseableStdoutLease {

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private PrintStream genuineStdOut;
  private PrintStream placeholder;

  @Override
  public CloseableStdoutLease startLease() {
    readWriteLock.writeLock().lock();
    try {
      if (genuineStdOut != null) {
        throw new IllegalStateException("A lease is already in progress");
      }
      genuineStdOut = System.out;
      placeholder =
          new PrintStream(
              new FailingOutputStream(
                  "System.out is currently monopolized by %s.".formatted(this)));
      System.setOut(placeholder);
      return this;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void write(byte[] buf) throws IOException {
    readWriteLock.readLock().lock();
    try {
      if (genuineStdOut == null) {
        throw new UnsupportedOperationException("No lease in progress");
      }
      genuineStdOut.write(buf);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void close() {
    readWriteLock.writeLock().lock();
    try {
      if (genuineStdOut == null) {
        return;
      }
      if (System.out.equals(placeholder)) {
        System.setOut(genuineStdOut);
      }
      genuineStdOut = null;
      placeholder = null;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }
}
