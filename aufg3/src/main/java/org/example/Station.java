package org.example;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Station {

    private static final int SYS_OUT_READER_CAPACITY = 20;
    private static final int SLOT_COUNT = 25;
    private static final int SLOT_DURATION_MS = 40;
    private final AtomicLong utcOffsetMs;
    private StationClass stationClass;
    private short nextSlot;
    private Frame slots = new Frame(SLOT_COUNT);

    private Thread receiver;
    private Thread sender;

    MulticastSocket sendSocket;
    MulticastSocket receiveSocket;

    SystemOutReader sysOutReader;

    public Station(String interfaceName,
                   String mcastAddress,
                   short receivePort,
                   StationClass stationClass,
                   long utcOffsetMs) {
        sysOutReader = new SystemOutReader(SYS_OUT_READER_CAPACITY);
        this.stationClass = stationClass;
        this.utcOffsetMs = new AtomicLong(utcOffsetMs);

        receiver = new Thread(this::receive);
        sender = new Thread(this::send);

        createSockets(interfaceName, mcastAddress, receivePort);
    }


    // ----------------------------------- USAGE -----------------------------------

    public void activate() {
        sysOutReader.start();
        receiver.start();
        sender.start();
    }

    // ----------------------------------- USAGE -----------------------------------

    // ----------------------------------- COMMUNICATION -----------------------------------

    private void receive() {
        while (true) {
            // TODO
        }
    }

    private void send() {
        while (true) {
            try {
                var dg = getDatagrammFromSrc();
                System.err.println("[Station] Sending datagramm:\n" + dg);

                // TODO

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ----------------------------------- COMMUNICATION -----------------------------------

    // ----------------------------------- PRIVATE -----------------------------------

    private void createSockets(String interfaceName, String addressString, short port) {
        try {
            InetAddress mcastaddr = InetAddress.getByName(addressString);
            SocketAddress group = new InetSocketAddress(mcastaddr, port);
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);

            // join multicast group
            sendSocket = new MulticastSocket(port);
            sendSocket.joinGroup(group, networkInterface);

            receiveSocket = new MulticastSocket(port);
            receiveSocket.joinGroup(group, networkInterface);

            sendSocket.setReuseAddress(true);
            sendSocket.setSoTimeout(SLOT_DURATION_MS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getTime() {
        return System.currentTimeMillis() + utcOffsetMs.get();
    }

    private Datagramm getDatagrammFromSrc() throws InterruptedException {
        byte[] data = sysOutReader.takeData();
        return new Datagramm(stationClass, data, (byte) this.nextSlot, getTime());
    }

    // ----------------------------------- PRIVATE -----------------------------------
}
