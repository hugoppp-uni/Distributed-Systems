package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SystemInReader extends Thread {

    byte[] mostRecentValue;
    byte[] readBuffer = new byte[24];

    public SystemInReader(int capacity) {
        super();
        mostRecentValue = new byte[capacity];
    }

    public byte[] takeData() throws InterruptedException {
        return mostRecentValue.clone();
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            readFromSystemIn(mostRecentValue);
        }
    }


    private void readFromSystemIn(byte[] data) {
        try {
            System.in.read(readBuffer, 0, 24);
            byte[] tmp = mostRecentValue;
            mostRecentValue = readBuffer;
            readBuffer = tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
