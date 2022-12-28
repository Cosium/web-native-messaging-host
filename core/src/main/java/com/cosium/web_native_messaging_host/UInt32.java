package com.cosium.web_native_messaging_host;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Réda Housni Alaoui
 */
class UInt32 {

  private final byte[] byteArray;

  public UInt32(byte[] byteArray) {
    this.byteArray = byteArray.clone();
  }

  public static UInt32 fromInt(int intValue) {
    ByteBuffer byteBuffer =
        ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.nativeOrder()).putInt(intValue).rewind();
    return new UInt32(byteBuffer.array());
  }

  public int toInt() {
    return ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder()).getInt();
  }

  public byte[] toByteArray() {
    return byteArray.clone();
  }
}
