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
        nextSlot+=1;//Fix slot numbering to be 1-indexed
        data.put(stationClass.toByte());
        for (int i = 0; i < USER_DATA_LENGTH; i++) {
            data.put(USER_DATA_INDEX + i, userData[i]);
        }
        data.put(NEXT_SLOT_INDEX, nextSlot);
    }


    StationClass getStationClass() {
        return data.get(STATION_NAME_INDEX) == 'A' ? StationClass.A : StationClass.B;
    }

    byte[] getTeamName(){
        return getUserData(10);
    }

    byte[] getUserData() {
        return getUserData(USER_DATA_LENGTH);
    }
    byte[] getUserData(int maxLength) {
        byte[] bytes = new byte[maxLength];
        for (int i = 0; i < maxLength; i++) {
            bytes[i] = data.get(USER_DATA_INDEX + i);
        }
        return bytes;
    }

    byte getNextSlot() {
        return (byte)(data.get(NEXT_SLOT_INDEX) - 1); //Fix slot numbering to be 1-indexed
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
        var userDataString = new String(getUserData(10), StandardCharsets.UTF_8);
        var nextSlot = getNextSlot();
        long sendTime = getSendTime();

        return String.format("%s|%s|%d|%d", stationClass, userDataString, nextSlot + 1, sendTime % 100_000);
    }

}

