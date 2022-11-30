package org.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.Frame.SLOT_DURATION_MS;

public class Station {

    private static final int SYS_OUT_READER_CAPACITY = 20;
    public static final int SLEEP_TOLLERANCE = 2;
    private long timeOffsetMs;
    private StationClass stationClass;
    private int sendSlot = -1;
    private Frame nextFrame;

    private Thread receiver;
    private Thread sender;

    MulticastSocket sendSocket;
    MulticastSocket receiveSocket;

    SystemOutReader sysOutReader;

    short port;
    SocketAddress group;
    InetAddress mcastAdress;
    NetworkInterface networkInterface;

    private AtomicInteger currentTimeSlot = new AtomicInteger(0);

    public Station(String interfaceName,
                   String mcastAddress,
                   short receivePort,
                   StationClass stationClass,
                   long timeOffset) {
        nextFrame = new Frame();
        sysOutReader = new SystemOutReader(SYS_OUT_READER_CAPACITY);
        this.stationClass = stationClass;
        this.timeOffsetMs = timeOffset;
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
            sendSocket.setReuseAddress(true);
            sendSocket.setSoTimeout(SLOT_DURATION_MS);

            receiveSocket = new MulticastSocket(port);
            receiveSocket.setNetworkInterface(networkInterface);
            receiveSocket.joinGroup(group, networkInterface);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void receive() {
        try {

            long lastReceiveTime = 0;
            int receivedInCurrentSlot = 0;
            STDMAPacket lastPacket = null;

            while (true) {


                try {
                    // receive packet on socket
                    byte[] data = new byte[STDMAPacket.BYTE_SIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                    receiveSocket.setSoTimeout((int) remainingTimeInSlot());
                    receiveSocket.receive(datagramPacket);
                    lastReceiveTime = getTime();
                    lastPacket = new STDMAPacket(data);
                    receivedInCurrentSlot += 1;
                } catch (SocketTimeoutException e) {
                    // slot is over
                    if (receivedInCurrentSlot == 1) {
                        nextFrame.setSlotOccupied(lastPacket.getNextSlot(), lastPacket.getStationClass());
                        if (currentTimeSlot.get() != sendSlot)
                            syncClock(lastPacket, lastReceiveTime);
                    } else if (receivedInCurrentSlot > 1) {
                        handleCollision();
                    } else {//no packet received
                    }

                    shiftToNextSlot();
                    lastPacket = null;
                    lastReceiveTime = 0;
                    receivedInCurrentSlot = 0;
                    sendCollision = false;
                    Thread.sleep(SLEEP_TOLLERANCE);
                }

            }
        } catch (IOException |
                 InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    boolean sendCollision = false;

    private void handleCollision() {
        if (currentTimeSlot.get() == sendSlot) {
            sendCollision = true;
            //todo collision while sending
        } else {

        }

    }

    private void send() {
        try {
            long millis = remainingTimeInFrame() + SLEEP_TOLLERANCE;
            System.err.println("Waiting rest of current slot (" + millis + "ms)");
            Thread.sleep(millis);
            millis = remainingTimeInFrame() + SLEEP_TOLLERANCE;
            System.err.println("Listening for one slot (" + millis + "ms)");
            Thread.sleep(millis);
            while (true) {

                byte[] data = sysOutReader.takeData();
                if (sendSlot < 0) {
                    sendSlot = nextFrame.getRandomFreeSlot();
                    System.err.println("Chose send-slot " + sendSlot);
                }


                while (currentTimeSlot.get() != sendSlot) {
                    Thread.sleep(remainingTimeInSlot() + SLEEP_TOLLERANCE);
                }
                Thread.sleep(ramaingingTimeUntilSlotMiddle() + SLEEP_TOLLERANCE);
                STDMAPacket packet = new STDMAPacket(stationClass, data, (byte) sendSlot);
                sendPacket(packet);
                System.err.println("Send packet in slot " + currentTimeSlot);
                Thread.sleep(remainingTimeInSlot() + SLEEP_TOLLERANCE);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    // ----------------------------------- COMMUNICATION -----------------------------------

    // ----------------------------------- PRIVATE -----------------------------------

    public void shiftToNextSlot() {
        int slot = currentTimeSlot.incrementAndGet();
        if (slot == Frame.SLOT_COUNT) {
            StringBuilder debugOutput = new StringBuilder().append(nextFrame)
                    .append(" offset: ").append(timeOffsetMs)
                    .append(", currTime: ").append(getTime() % 100_000);
            if (sendCollision)
                debugOutput.append(" SEND COLLISION");

            System.err.println(debugOutput);
            nextFrame.resetSlots();
            currentTimeSlot.set(0);
        }
    }

    private long getTime() {
        return System.currentTimeMillis() + timeOffsetMs;
    }

    private void setTimeTo(long time) {
        long delta = time - getTime();
        timeOffsetMs += delta / 2;
    }

    private long remainingTimeInSlot() {
        long timeSpendInSlot = getTime() % SLOT_DURATION_MS;
        return SLOT_DURATION_MS - timeSpendInSlot;
    }

    private void syncClock(STDMAPacket packet, long receiveTime) {
        if (packet.getStationClass() != StationClass.A) {
            return;
        }

        if (packet.getStationClass() == StationClass.A) {
            long deltaTSinceReceive = getTime() - receiveTime;
            long adjustedPacketTime = packet.getSendTime() + deltaTSinceReceive;
            setTimeTo(adjustedPacketTime);
        }
    }

    private long remainingTimeInFrame() {
        long timeSpendInFrame = getTime() % Frame.DURATION_MS;
        return Frame.DURATION_MS - timeSpendInFrame;
    }

    private long ramaingingTimeUntilSlotMiddle() {
        long time = remainingTimeInSlot() - SLOT_DURATION_MS / 2;
        return time < 0 ? 0 : time;
    }

    private void sendPacket(STDMAPacket packet) throws IOException {
        packet.setSendTime(getTime());
        sendSocket.send(new DatagramPacket(packet.toByteArray(), STDMAPacket.BYTE_SIZE, mcastAdress, port));
    }

    // ----------------------------------- PRIVATE -----------------------------------
}
