package org.example;

import org.junit.jupiter.api.Test;

import static org.example.Datagramm.*;
import static org.junit.jupiter.api.Assertions.*;

class DatagrammTest {
    @Test
    void TestDatagram() {
        var data = new byte[24];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 'a');
        }

        long sendTime = 123415;
        byte nextSlot = 8;
        StationClass stationClass = StationClass.A;
        Datagramm datagramm = new Datagramm(stationClass, data, nextSlot, sendTime);

        assertEquals(sendTime, datagramm.getSendTime());
        assertEquals(nextSlot, datagramm.getNextSlot());
        assertEquals(stationClass, datagramm.getStationClass());
        assertArrayEquals(data, datagramm.getUserData());
        System.out.println(datagramm.toString());


    }

}