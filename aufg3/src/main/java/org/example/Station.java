package org.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class Station {

    private static final int SYS_OUT_READER_CAPACITY = 20;
    private final AtomicLong utcOffsetMs;
    private StationClass stationClass;
    private int nextSlot = 1;
    private short currentSlot;
    private Frame frame;

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
        frame = new Frame();
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
            sendSocket.setSoTimeout(Frame.SLOT_DURATION_MS);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void receive() {
        try {
            while (true) {
                Datagram dg = receiveDatagram();
                System.err.println("[RECEIVER] received:\n" + dg);

                frame.setSlotOccupied(frame.getCurrentTimeSlot(), stationClass);
                System.err.println(frame);
                frame.freeSlot(frame.getCurrentTimeSlot());
                frame.shift();
                nextSlot = frame.getCurrentTimeSlot() + 1;

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

                Thread.sleep(Frame.DURATION_MS);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------- COMMUNICATION -----------------------------------

    // ----------------------------------- PRIVATE -----------------------------------

    private long getTime() {
        return System.currentTimeMillis() + utcOffsetMs.get();
    }

    private Datagram getDatagrammFromSrc() throws InterruptedException {
        byte[] data = sysOutReader.takeData();
        return new Datagram(stationClass, data, (byte) this.nextSlot, getTime());
    }

    private void sendDatagram(Datagram dg) throws IOException {
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
