package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatagramTest {
    @Test
    void TestDatagram() {
        var data = new byte[24];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 'a');
        }

        long sendTime = 123415;
        byte nextSlot = 8;
        StationClass stationClass = StationClass.A;
        Datagram datagram = new Datagram(stationClass, data, nextSlot, sendTime);

        assertEquals(sendTime, datagram.getSendTime());
        assertEquals(nextSlot, datagram.getNextSlot());
        assertEquals(stationClass, datagram.getStationClass());
        assertArrayEquals(data, datagram.getUserData());
        System.out.println(datagram.toString());


    }

}