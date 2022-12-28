package com.cosium.web_native_messaging_host;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
class UInt32Test {

  @Test
  void test1() {
    assertThat(new UInt32(new byte[] {12, 0, 0, 0}).toInt()).isEqualTo(12);
  }

  @Test
  void test2() {
    assertThat(new UInt32(UInt32.fromInt(20).toByteArray()).toInt()).isEqualTo(20);
  }
}
