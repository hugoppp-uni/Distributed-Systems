package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class SystemOutReader extends Thread {

    private final BlockingQueue<byte[]> msgBuffer;
    private final ByteArrayOutputStream out;

    public SystemOutReader(BlockingQueue<byte[]> buffer) {
        super();

        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        msgBuffer = buffer;
    }

    // TODO critical region if data src period is too small
    @Override
    public void run() {
        byte[] userData = new byte[24];
        while(!Thread.currentThread().isInterrupted()) {

            if(out.size() == 0) continue;
            byte[] data = out.toByteArray();

            try {
                // put into message buffer
                msgBuffer.put(data);

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
