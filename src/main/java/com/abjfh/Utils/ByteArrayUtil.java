package com.abjfh.Utils;

import lombok.NonNull;

public class ByteArrayUtil {
    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    // 预先生成 0~255 所有字节的二进制位数组（静态初始化，避免重复计算）
    private static final byte[][] BIT_CACHE = new byte[256][8];

    static {
        for (int i = 0; i < 256; i++) {
            for (int j = 7; j >= 0; j--) {
                BIT_CACHE[i][7 - j] = (byte) ((i >> j) & 1);
            }
        }
    }

    public static byte[] mask2ByteArray(byte[] ipAddress, int prefixLength) {

        checkNetwork(ipAddress, prefixLength);

        byte[] prefix = new byte[prefixLength];
        int fullBytes = prefixLength / 8;      // 完整字节数（每字节8位）
        int remainingBits = prefixLength & 7;  // 剩余位数
        int position = 0;                      // 当前填充位置

        // 1. 处理完整的字节
        for (int i = 0; i < fullBytes; i++) {
            byte address = ipAddress[i];
            int unsignedByte = address & 0xFF;  // 转换为无符号整数（0~255）
            // 直接复制预计算的二进制位数组
            System.arraycopy(BIT_CACHE[unsignedByte], 0, prefix, position, 8);
            position += 8;
        }

        // 2. 处理剩余位数
        if (remainingBits > 0) {
            byte address = ipAddress[fullBytes];
            int unsignedByte = address & 0xFF;
            // 仅复制剩余位数
            System.arraycopy(BIT_CACHE[unsignedByte], 0, prefix, position, remainingBits);
        }

        return prefix;
    }

    public static void checkNetwork(@NonNull byte[] ipAddress, @NonNull Integer prefixLength) {

        if (prefixLength < 0) {
            throw new IllegalArgumentException("Prefix length must be greater than zero");
        }
        switch (ipAddress.length) {
            case 0:
                throw new IllegalArgumentException("IP address byte arr is empty");
            case 4: {
                if ((prefixLength > 32)) {
                    throw new IllegalArgumentException("IPV4 address prefixLength must be less than 32");
                }
                break;
            }
            case 16: {
                if ((prefixLength > 128)) {
                    throw new IllegalArgumentException("IPV4 address prefixLength must be less than 128");
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("IP address byte arr length is not valid, must be 4 or 32");
            }
        }
    }
}
