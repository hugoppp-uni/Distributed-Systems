package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SystemOutReader extends Thread {

    private final BlockingQueue<byte[]> buffer;
    private final ByteArrayOutputStream out;

    public SystemOutReader(int capacity) {
        super();
        buffer = new ArrayBlockingQueue<>(capacity);
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

    }

    public byte[] takeData() throws InterruptedException {
        return buffer.take();
    }

    // TODO might clear more than 24 bytes w/ reset, worked so far w/ 100 ms data source period and given capacity
    @Override
    public void run() {
        byte[] userData = new byte[24];
        while(!Thread.currentThread().isInterrupted()) {

            if(out.size() == 0) continue;
            byte[] data = out.toByteArray();

            try {
                // put into message buffer
                buffer.put(data);

                // log
                System.err.print("[SystemOutReader] Read " + data.length + " bytes from out: ");
                System.err.write(data);
                System.err.println();

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            out.reset();
        }
    }
}
