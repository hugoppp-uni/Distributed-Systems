package org.example;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
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

    short port;
    SocketAddress group;
    InetAddress mcastAdress;
    NetworkInterface networkInterface;

    public Station(String interfaceName,
                   String mcastAddress,
                   short receivePort,
                   StationClass stationClass,
                   long utcOffsetMs) {
        sysOutReader = new SystemOutReader(SYS_OUT_READER_CAPACITY);
        this.stationClass = stationClass;
        this.utcOffsetMs = new AtomicLong(utcOffsetMs);
        this.port = receivePort;

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
        try {
            while (true) {

                Datagram dg = null;
                dg = receiveDatagram();
                System.err.println("[RECEIVER] received:\n" + dg);

                // TODO

            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void send() {
        try {
            while (true) {

                sendDatagram(getDatagrammFromSrc());

                // TODO

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------- COMMUNICATION -----------------------------------

    // ----------------------------------- SET UP COMMUNICATION -----------------------------------

    private void createSockets(String interfaceName, String addressString, short port) {
        try {
            mcastAdress = InetAddress.getByName(addressString);
            group = new InetSocketAddress(mcastAdress, port);
            networkInterface = NetworkInterface.getByName(interfaceName);

            // join multicast group
            sendSocket = new MulticastSocket(port);
            sendSocket.setNetworkInterface(networkInterface);
            sendSocket.joinGroup(group, networkInterface);

            receiveSocket = new MulticastSocket(port);
            receiveSocket.setNetworkInterface(networkInterface);
            receiveSocket.joinGroup(group, networkInterface);

            sendSocket.setReuseAddress(true);
            sendSocket.setSoTimeout(SLOT_DURATION_MS);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // ----------------------------------- SET UP COMMUNICATION -----------------------------------

    // ----------------------------------- PRIVATE -----------------------------------

    private long getTime() {
        return System.currentTimeMillis() + utcOffsetMs.get();
    }

    private Datagram getDatagrammFromSrc() throws InterruptedException {
        byte[] data = sysOutReader.takeData();
        return new Datagram(stationClass, data, (byte) this.nextSlot, getTime());
    }

    private void sendDatagram(Datagram dg) throws IOException {
//        System.err.println("[SENDER] Sending datagramm:\n" + dg);
        sendSocket.send(new DatagramPacket(dg.toByteArray(), Datagram.DG_SIZE, mcastAdress, port));
    }

    private Datagram receiveDatagram() throws IOException {
        byte[] data = new byte[Datagram.DG_SIZE];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        receiveSocket.receive(packet);
        return new Datagram(data);
    }

    // ----------------------------------- PRIVATE -----------------------------------
}
