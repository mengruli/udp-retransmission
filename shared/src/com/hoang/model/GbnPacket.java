package model;

import java.nio.ByteBuffer;

public class GbnPacket {
    public static final short BUFSIZE = 1024;
    public static final short WINDOW_SIZE = 4;
    public static final int TIMEOUT_LIMIT = 50;

    public static void printByteBuffer(ByteBuffer buffer) {

        short seq = buffer.getShort(0);
        short winSize = buffer.getShort(2);
        char type = (char)buffer.getShort(4);
        short len = buffer.getShort(6);

        System.out.println(String.format("Seq#: %d\tWindow Size: %d\tType: %c\tLength: %d", seq, winSize, type, len));
    }
}
