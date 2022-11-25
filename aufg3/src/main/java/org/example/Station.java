package org.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.Datagramm.*;

public class Station {

    private static final int SLOT_COUNT = 25;
    private static final int SLOT_DURATION_MS = 40;
    private final AtomicLong clockOffset;
    private StationClass stationClass;
    private Frame slots = new Frame(SLOT_COUNT);

    public Station(StationClass stationClass, String address, short port, long clockOffset) {
        this.stationClass = stationClass;
        this.clockOffset = new AtomicLong(clockOffset);
        createSockets(address, port);
    }

    public void createSockets(String addressString, short port) {

        try {
            InetAddress mcastaddr = InetAddress.getByName(addressString);
            SocketAddress group = new InetSocketAddress(mcastaddr, port);
            NetworkInterface networkInterface = NetworkInterface.getByIndex(0);

            // join multicast group
            MulticastSocket sendSocket = new MulticastSocket(port);
            sendSocket.joinGroup(group, networkInterface);
            sendSocket.setReuseAddress(true);

            MulticastSocket receiveSocket = new MulticastSocket(port);
            receiveSocket.joinGroup(group, networkInterface);
            sendSocket.setReuseAddress(true);
            sendSocket.setSoTimeout(SLOT_DURATION_MS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveLoop() {
        while(true) {

        }
    }

    private void sendLoop() {
        while(true) {

        }
    }

    private long getTime() {

        return System.currentTimeMillis() + clockOffset.get();
    }

}
