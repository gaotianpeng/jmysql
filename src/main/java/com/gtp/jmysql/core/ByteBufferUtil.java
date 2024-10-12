package com.gtp.jmysql.core;

import cn.hutool.core.util.ByteUtil;

import java.nio.ByteBuffer;

public class ByteBufferUtil {
    /*
        写1字节n到byteBuffer的指定位置p
     */
    public static void mach_write_to_1(ByteBuffer byteBuffer, int p, int n) {
        byte[] b = new byte[1];
        b[0] = (byte)(n);
        byteBuffer.position(p);
        byteBuffer.put(b);
        byteBuffer.clear();
    }
    /*
        从byteBuffer指定位置p读出一个字节数据返回
     */
    public static int mach_read_from_1(ByteBuffer byteBuffer, int p) {
        byte[] b = new byte[1];
        byteBuffer.position(p);
        byteBuffer.get(b);
        byteBuffer.clear();
        return b[0];
    }

    public static void mach_write_to_2(ByteBuffer byteBuffer, int p, int n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n >> 8);
        b[1] = (byte) (n);
        byteBuffer.position(p);
        byteBuffer.put(b);
        byteBuffer.clear();
    }

    public static int mach_read_from_2(ByteBuffer byteBuffer, int p) {
        byte[] b = new byte[2];
        byteBuffer.position(p);
        byteBuffer.get(b);
        byteBuffer.clear();
        return (((int) (b[0]) << 8) | (int) (ByteUtil.byteToUnsignedInt(b[1])));
    }

    public static void mach_write_to_4(ByteBuffer byteBuffer, int p, int n) {
        byte[] bytes = ByteUtil.intToBytes(n);
        byteBuffer.position(p);
        byteBuffer.put(bytes);
        byteBuffer.clear();
    }

    public static int mach_read_from_4(ByteBuffer byteBuffer, int p) {
        byte[] b = new byte[4];
        byteBuffer.position(p);
        byteBuffer.get(b);
        byteBuffer.clear();
        return ByteUtil.bytesToInt(b);
    }

    public static String mach_read_from_size_str(ByteBuffer byteBuffer, int p, int size) {
        byte[] b = new byte[size];

        byteBuffer.position(p);
        byteBuffer.get(b);
        byteBuffer.clear();

        return new String(b);
    }

    public static void memcpy(ByteBuffer byteBuffer, int p, byte[] b) {
        byteBuffer.position(p);
        byteBuffer.put(b);
        byteBuffer.clear();
    }
}
