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
class SystemStdinTest {

  private SystemStdin systemStdin;

  @BeforeEach
  void beforeEach() {
    systemStdin = new SystemStdin();
  }

  @AfterEach
  void afterEach() {
    systemStdin.close();
  }

  @Test
  @DisplayName("During the lease, System.in usage fails")
  void test1() {
    try (CloseableStdinLease ignored = systemStdin.startLease()) {
      assertThatThrownBy(System.in::available)
          .hasMessageContaining("System.in is currently monopolized by");
    }
  }

  @Test
  @DisplayName("After the lease, System.in usage do not fail")
  void test2() {
    systemStdin.startLease().close();
    assertThatCode(System.in::available).doesNotThrowAnyException();
  }
}
