package org.example;

import static org.example.Frame.DURATION_MS;
import static org.example.Frame.SLOT_DURATION_MS;

public class STDMATime {

    private long timeOffsetMs;

    public STDMATime(long timeOffset) {
        this.timeOffsetMs = timeOffset;
    }

    public long get() {
        return System.currentTimeMillis() + timeOffsetMs;
    }

    public void setTimeTo(long time) {
        long delta = time - get();
        timeOffsetMs += delta / 2;
    }
    public long remainingMsInSlot() {
        long timeSpendInSlot = get() % SLOT_DURATION_MS;
        return SLOT_DURATION_MS - timeSpendInSlot;
    }

    public long timestampAt(long frame, int slot) {
        long timestampSlotBegin = (frame * DURATION_MS) + (SLOT_DURATION_MS *slot);
        return timestampSlotBegin;
    }

    public int getCurrentSlot(){
        return (int) ((get() % DURATION_MS) / SLOT_DURATION_MS);
    }

    public long getCurrentFrame(){
        return (get() / DURATION_MS);
    }

    public void sync(STDMAPacket packet, long receiveTime) {
        if (packet.getStationClass() != StationClass.A) {
            return;
        }

        long deltaTSinceReceive = get() - receiveTime;
        long adjustedPacketTime = packet.getSendTime() + deltaTSinceReceive;
        setTimeTo(adjustedPacketTime);
    }

    public long remainingTimeInFrame() {
        long timeSpendInFrame = get() % Frame.DURATION_MS;
        return Frame.DURATION_MS - timeSpendInFrame;
    }

    public long remainingTimeUntilSlotMiddle() {
        long time = remainingMsInSlot() - SLOT_DURATION_MS / 2;
        return time < 0 ? 0 : time;
    }

    public long getMsOffset() {
        return timeOffsetMs;
    }
}
