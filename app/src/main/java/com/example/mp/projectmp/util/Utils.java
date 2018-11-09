package com.example.mp.projectmp.util;

import java.nio.ByteBuffer;

public class Utils {
    public static byte[] longToByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    static long byteArrayToLong(byte[] array) {
        return ByteBuffer.wrap(array).getLong();
    }
}
