package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class STDMAPacket {

    public static final int BYTE_SIZE = 34;
    private static final int STATION_NAME_INDEX = 0;
    private static final int USER_DATA_INDEX = 1;
    private static final int USER_DATA_LENGTH = 24;
    private static final int NEXT_SLOT_INDEX = 25;
    private static final int SEND_TIME_INDEX = 26;

    private ByteBuffer data = ByteBuffer.allocate(BYTE_SIZE);

    public STDMAPacket(byte[] data) {
        assert(data.length == BYTE_SIZE);
        this.data = ByteBuffer.wrap(data.clone());
    }

    public STDMAPacket(StationClass stationClass, byte[] userData, byte nextSlot) {
        data.put(stationClass.toByte());
        data.put(USER_DATA_INDEX, userData, 0, USER_DATA_LENGTH);
        data.put(NEXT_SLOT_INDEX, nextSlot);
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

    void setSendTime(long sendTime) {
        data.putLong(SEND_TIME_INDEX, sendTime);
    }

    public byte[] toByteArray() {
        return data.array();
    }

    public STDMAPacket(ByteBuffer data) {
        this.data = data;
    }

    @Override
    public String toString() {
        var stationClass = getStationClass().toString();
        var userDataString = new String(getUserData(), StandardCharsets.UTF_8);
        var nextSlot = getNextSlot();
        long sendTime = getSendTime();

        return String.format("%s|%s|%d|%d", stationClass, userDataString, nextSlot, sendTime % 100_000);
    }

}

