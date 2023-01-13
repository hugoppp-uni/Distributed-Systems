package org.example;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.Frame.SLOT_DURATION_MS;

public class Station {

    public static final int SLEEP_TOLLERANCE = 4;
    private StationClass stationClass;
    private int sendSlot = -1;
    private Frame nextFrame;
    private long nextSendFrame = 0;

    private Thread receiver;
    private Thread sender;

    MulticastSocket sendSocket;
    MulticastSocket receiveSocket;

    SystemInReader sysInReader;

    short port;
    SocketAddress group;
    InetAddress mcastAdress;
    NetworkInterface networkInterface;
    STDMATime time;

    private AtomicInteger currentTimeSlot = new AtomicInteger(0);

    private final Random random = new Random();

    final DatagramPacket receiveDatagram = new DatagramPacket(new byte[STDMAPacket.BYTE_SIZE], STDMAPacket.BYTE_SIZE);

    public Station(String interfaceName,
                   String mcastAddress,
                   short receivePort,
                   StationClass stationClass,
                   long timeOffset) {
        nextFrame = new Frame();
        sysInReader = new SystemInReader(STDMAPacket.BYTE_SIZE);
        this.stationClass = stationClass;
        time = new STDMATime(timeOffset);
        this.port = receivePort;

        receiver = new Thread(this::receive);
        sender = new Thread(this::send);

        createSockets(interfaceName, mcastAddress, receivePort);
    }


    // ----------------------------------- USAGE -----------------------------------
    public void activate() {
        sysInReader.start();
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
            Thread.sleep(time.remainingMsInSlot() + SLEEP_TOLLERANCE);
            currentTimeSlot .set(time.getCurrentSlot());

            while (true) {
                try {
                    // receive packet on socket
                    receiveSocket.setSoTimeout((int) time.remainingMsInSlot() + SLEEP_TOLLERANCE);
                    receiveSocket.receive(receiveDatagram);
                    lastReceiveTime = time.get();
                    lastPacket = new STDMAPacket(receiveDatagram.getData());
                    syncLog(lastPacket.getSendTime() + " (slot " + (getCurrentTimeSlotString()) + "): received '" + lastPacket);
                    receivedInCurrentSlot += 1;
                } catch (SocketTimeoutException e) { // slot is over

                    if (receivedInCurrentSlot > 0) { // somebody sent in this slot
                        nextFrame.setSlotOccupied(lastPacket.getNextSlot(), lastPacket.getStationClass());
                    }

                    if (receivedInCurrentSlot == 1) {
                        if (currentTimeSlot.get() != sendSlot) {
                            time.sync(lastPacket, lastReceiveTime);
                        }
                    }

                    if (receivedInCurrentSlot > 1) {
                        if (lastPacket != null) {
                            nextFrame.setSlotCollision(lastPacket.getNextSlot()); // debugging purpose
                        }
                        handleCollision();
                    }

                    shiftToNextSlot();

                    lastPacket = null;
                    lastReceiveTime = 0;
                    receivedInCurrentSlot = 0;
                }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private void send() {
        try {
            long millis = time.remainingTimeInFrame() + SLEEP_TOLLERANCE;
            syncLog("Waiting rest of current frame (" + millis + "ms)");
            Thread.sleep(millis);
            sendSlot = -2;
            millis = time.remainingTimeInFrame() + SLEEP_TOLLERANCE;
            syncLog("Listening for one slot (" + millis + "ms)");
            Thread.sleep(millis);
            nextSendFrame = time.getCurrentFrame();

            while (true) {

                // wait for time slot
                long timestampWhenToSend = time.timestampAt(nextSendFrame, sendSlot) + SLEEP_TOLLERANCE;
                while (timestampWhenToSend >= time.get()){
                    Thread.sleep(1);
                }
                syncLog("were in " + getCurrentTimeSlotString() + " :" + time.get());
                Thread.sleep(time.remainingTimeUntilSlotMiddle());
                syncLog("were in " + getCurrentTimeSlotString() + " :" + time.get());


                // send data
                byte[] data = sysInReader.takeData();

                //choose when to send in the next slot
                int nextSendSlot = nextFrame.getRandomFreeSlot();
                STDMAPacket packet = new STDMAPacket(stationClass, data, (byte) nextSendSlot);
                syncLog("Planned sending in slot:" + (sendSlot + 1) + "Sending in slot " + (getCurrentTimeSlotString()) + ": " + packet);
                sendPacket(packet);

                String debugOutput = "Next frame: " + nextFrame +
                        " offset: " + time.getMsOffset() +
                        ", currTime: " + time.get() ;
                syncLog(debugOutput);

                sendSlot = nextSendSlot;
                syncLog("Chose next slot" + sendSlot + 1);

                nextSendFrame++;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    // ----------------------------------- COMMUNICATION -----------------------------------

    // ----------------------------------- PRIVATE -----------------------------------

    private void syncLog(String s) {
        synchronized (System.err) {
            System.err.println(s);
        }
    }

    private String getCurrentTimeSlotString(){
        return ""  + (currentTimeSlot.get() + 1);
    }

    private void handleCollision() {
        if (currentTimeSlot.get() == sendSlot) {
            syncLog("Collision while sending in slot " + getCurrentTimeSlotString() + 1);
            nextFrame.setSlotUnoccupied(sendSlot);
            sendSlot = -2;
        } else {
            //todo
        }
    }

    public void shiftToNextSlot() {
        currentTimeSlot.set(time.getCurrentSlot());

        // frame over
        if (currentTimeSlot.get() == 0) {

            if (sendSlot == -2){
                sendSlot = nextFrame.getRandomFreeSlot();
                syncLog("Choosing slot after whole frame: " + sendSlot);
            }
            nextFrame.resetSlots();

            syncLog("======================== " + time.get() / 1000 + " ========================");
        }

    }

    private void sendPacket(STDMAPacket packet) throws IOException {
        packet.setSendTime(time.get());
        sendSocket.send(new DatagramPacket(packet.toByteArray(), STDMAPacket.BYTE_SIZE, mcastAdress, port));
    }

    // ----------------------------------- PRIVATE -----------------------------------
}