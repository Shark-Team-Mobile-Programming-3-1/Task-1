package com.example.task1.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/** Returns (trusted time - device time) in milliseconds. Blocking: call from a background thread. */
public final class SntpClient {

    private static final int NTP_PORT = 123;
    private static final long EPOCH_1900_TO_1970_SEC = 2_208_988_800L;

    private SntpClient() {}

    public static long fetchOffsetMs(String host, int timeoutMs) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeoutMs);
            byte[] buf = new byte[48];
            buf[0] = 0x1B;                                   // NTP client request
            InetAddress address = InetAddress.getByName(host);

            long t0 = System.currentTimeMillis();
            socket.send(new DatagramPacket(buf, buf.length, address, NTP_PORT));
            socket.receive(new DatagramPacket(buf, buf.length));
            long t3 = System.currentTimeMillis();

            long t1 = readTimestamp(buf, 32);                // server receive time
            long t2 = readTimestamp(buf, 40);                // server transmit time
            return ((t1 - t0) + (t2 - t3)) / 2;              // standard NTP offset formula
        }
    }

    private static long readTimestamp(byte[] b, int off) {
        long sec = 0, frac = 0;
        for (int i = 0; i < 4; i++) sec = (sec << 8) | (b[off + i] & 0xFF);
        for (int i = 4; i < 8; i++) frac = (frac << 8) | (b[off + i] & 0xFF);
        return (sec - EPOCH_1900_TO_1970_SEC) * 1000L + ((frac * 1000L) >> 32);
    }
}
