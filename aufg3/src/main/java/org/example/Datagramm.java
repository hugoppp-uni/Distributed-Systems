package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Datagramm {

    private static final int DG_SIZE = 34;
    private static final int STATION_NAME_INDEX = 0;
    private static final int USER_DATA_INDEX = 1;
    private static final int USER_DATA_LENGTH = 24;
    private static final int NEXT_SLOT_INDEX = 25;
    private static final int SEND_TIME_INDEX = 26;

    ByteBuffer data = ByteBuffer.allocate(DG_SIZE);

    public Datagramm(StationClass stationClass, byte[] userData, byte nextSlot, long sendTime) {
        data.put(stationClass.toByte());
        data.put(USER_DATA_INDEX, userData, 0, USER_DATA_LENGTH);
        data.put(NEXT_SLOT_INDEX, nextSlot);
        data.putLong(SEND_TIME_INDEX, sendTime);
    }


    StationClass getStationClass() {
        return data.get(STATION_NAME_INDEX) == 'A' ? StationClass.A : StationClass.B;
    }

    byte[] getUserData() {
        byte[] bytes = new byte[USER_DATA_LENGTH];
        data.get(USER_DATA_INDEX, bytes, 0, USER_DATA_LENGTH);
        return bytes;
    }

    byte getNextSlot() {
        return data.get(NEXT_SLOT_INDEX);
    }

    long getSendTime() {
        return data.getLong(SEND_TIME_INDEX);
    }

    public enum StationClass {
        A, B;

        public byte toByte() {
            return (byte) this.toString().charAt(0);
        }
    }

    public Datagramm(ByteBuffer data) {
        this.data = data;
    }

    @Override
    public String toString() {
        var stationClass = getStationClass().toString();
        var userDataString = new String(getUserData(), StandardCharsets.UTF_8);
        var nextSlot = getNextSlot();
        long sendTime = getSendTime();

        return String.format("Class: %s\r\nData: %s\r\nNext Slot: %d\r\nSend Time: %d",stationClass,userDataString,nextSlot,sendTime);
    }

}

