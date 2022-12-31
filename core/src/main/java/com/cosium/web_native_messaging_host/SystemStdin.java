package com.cosium.web_native_messaging_host;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Réda Housni Alaoui
 */
class SystemStdin implements Stdin, CloseableStdinLease {
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private InputStream genuineStdIn;
  private InputStream placeholder;

  @Override
  public CloseableStdinLease startLease() {
    readWriteLock.writeLock().lock();
    try {
      if (genuineStdIn != null) {
        throw new IllegalStateException("A lease is already in progress");
      }
      genuineStdIn = System.in;
      placeholder =
          new FailingInputStream("System.in is currently monopolized by %s.".formatted(this));
      System.setIn(placeholder);

      return this;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public int available() throws IOException {
    readWriteLock.readLock().lock();
    try {
      if (genuineStdIn == null) {
        throw new UnsupportedOperationException("No lease in progress");
      }
      return genuineStdIn.available();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public int read() throws IOException {
    readWriteLock.readLock().lock();
    try {
      if (genuineStdIn == null) {
        throw new UnsupportedOperationException("No lease in progress");
      }
      return genuineStdIn.read();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public int read(byte[] buffer, int off, int len) throws IOException {
    readWriteLock.readLock().lock();
    try {
      if (genuineStdIn == null) {
        throw new UnsupportedOperationException("No lease in progress");
      }
      return genuineStdIn.read(buffer, off, len);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public byte[] readNBytes(int len) throws IOException {
    readWriteLock.readLock().lock();
    try {
      if (genuineStdIn == null) {
        throw new UnsupportedOperationException("No lease in progress");
      }
      return genuineStdIn.readNBytes(len);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void close() {
    readWriteLock.writeLock().lock();
    try {
      if (genuineStdIn == null) {
        return;
      }
      if (System.in.equals(placeholder)) {
        System.setIn(genuineStdIn);
      }
      genuineStdIn = null;
      placeholder = null;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }
}
