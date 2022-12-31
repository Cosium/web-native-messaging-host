package com.cosium.web_native_messaging_host;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Réda Housni Alaoui
 */
class SystemErrLogger implements Logger {

  @Override
  public void debug(String message, Throwable throwable) {
    print("DEBUG", message, throwable);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    print("WARN", message, throwable);
  }

  @Override
  public void error(String message, Throwable throwable) {
    print("ERROR", message, throwable);
  }

  /**
   * @param level Never null
   * @param message Can be null
   * @param throwable Can be null
   */
  private void print(String level, String message, Throwable throwable) {
    requireNonNull(level, "level is mandatory");
    String log =
        Stream.of(
                "[%s]".formatted(Thread.currentThread().getName()),
                level,
                SystemErrLogger.class.getCanonicalName(),
                "-",
                message)
            .filter(Objects::nonNull)
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.joining(" "));
    System.err.print(log);
    Optional.ofNullable(throwable)
        .ifPresentOrElse(
            t -> {
              System.err.print(" ");
              t.printStackTrace(System.err);
            },
            System.err::println);
  }
}
