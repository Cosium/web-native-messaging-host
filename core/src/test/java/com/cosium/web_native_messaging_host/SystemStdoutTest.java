package com.cosium.web_native_messaging_host;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
class SystemStdoutTest {

  private SystemStdout systemStdout;

  @BeforeEach
  void beforeEach() {
    systemStdout = new SystemStdout();
  }

  @AfterEach
  void afterEach() {
    systemStdout.close();
  }

  @Test
  @DisplayName("During the lease, System.out usage fails")
  void test1() {
    try (CloseableStdoutLease ignored = systemStdout.startLease()) {
      assertThatThrownBy(() -> System.out.println("foo"))
          .hasMessageContaining("System.out is currently monopolized by");
    }
  }

  @Test
  @DisplayName("After the lease, System.out usage do not fail")
  void test2() {
    systemStdout.startLease().close();
    assertThatCode(() -> System.out.println("foo")).doesNotThrowAnyException();
  }
}
