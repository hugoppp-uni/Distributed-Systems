package org.example;

import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    private static final boolean FOREVER = true;
    private static final int DATA_SOURCE_OUTPUT_PERIOD = 5000;
    private static final int DATA_SOURCE_INITIAL_DELAY = 100;
    private static final int QUEUE_CAPACITY = 5;

    public static void main(String[] args) {
        // schedule data source
        DataSource dataSource = new DataSource();
        Timer outputTimer = new Timer(true);
        outputTimer.scheduleAtFixedRate(dataSource, DATA_SOURCE_INITIAL_DELAY, DATA_SOURCE_OUTPUT_PERIOD);

        // init system out reader
        BlockingQueue<byte[]> msgBuffer = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        SystemOutReader inputReader = new SystemOutReader(msgBuffer);
        inputReader.start();

        while (FOREVER) {

            if(msgBuffer.size() == 0) continue;
            try {
                // read bytes from msgBuffer
                byte[] inputData = msgBuffer.take();

                // assemble into datagramm
                StationClass stationClass = StationClass.A;
                byte nextSlot = (byte) 25;
                long timestamp = System.currentTimeMillis();
                Datagramm packet = new Datagramm(stationClass, inputData, nextSlot, timestamp);

                // log
                System.err.println("[MAIN] Datagramm: " + packet.toString());

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}