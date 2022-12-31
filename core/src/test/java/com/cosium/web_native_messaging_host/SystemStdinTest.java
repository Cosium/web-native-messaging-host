package com.cosium.web_native_messaging_host;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
class SystemStdinTest {

  private MySystemFacade systemFacade;
  private SystemStdin systemStdin;

  @BeforeEach
  void beforeEach() {
    systemFacade = new MySystemFacade();
    systemStdin = new SystemStdin(systemFacade);
  }

  @AfterEach
  void afterEach() {
    systemStdin.close();
  }

  @Test
  @DisplayName("During the lease, System.in usage fails")
  void test1() {
    try (CloseableStdinLease ignored = systemStdin.startLease()) {
      assertThatThrownBy(systemFacade.in::available)
          .hasMessageContaining("System.in is currently monopolized by");
    }
  }

  @Test
  @DisplayName("After the lease, System.in usage do not fail")
  void test2() {
    systemStdin.startLease().close();
    assertThatCode(systemFacade.in::available).doesNotThrowAnyException();
  }

  private static class MySystemFacade implements SystemStdin.SystemFacade {

    private InputStream in = new ByteArrayInputStream(new byte[0]);

    @Override
    public InputStream in() {
      return in;
    }

    @Override
    public void setIn(InputStream inputStream) {
      this.in = inputStream;
    }
  }
}
