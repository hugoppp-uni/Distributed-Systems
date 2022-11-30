package org.example;

import java.util.Arrays;

public class Frame {


    public static final int DURATION_MS = 1000;
    public static final int SLOT_COUNT = 25;

    public static final int SLOT_DURATION_MS = DURATION_MS / SLOT_COUNT;

    public enum SlotStatus {
        OCCUPIED_A("A"),
        OCCUPIED_B("B"),
        UNOCCUPIED("."),
        COLLISION("X");

        private final String s;

        private SlotStatus(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    private final SlotStatus[] slots;
    private int currentTimeSlot;

    public Frame() {
        slots = new SlotStatus[SLOT_COUNT];
        resetSlots();
        currentTimeSlot = 1;
    }

    public synchronized boolean slotIsFree(int slotNumber) {
        return slots[slotNumber - 1] == SlotStatus.UNOCCUPIED;
    }

    public synchronized void setSlotOccupied(int slotNumber, StationClass stationClass) {
        SlotStatus occupant = stationClass == StationClass.A ? SlotStatus.OCCUPIED_A : SlotStatus.OCCUPIED_B;
        slots[slotNumber - 1] = slots[slotNumber - 1] != SlotStatus.UNOCCUPIED ? SlotStatus.COLLISION : occupant;
    }

    public synchronized void freeSlot(int slotNumber) {
        slots[slotNumber - 1] = SlotStatus.UNOCCUPIED;
    }

    public int getCurrentTimeSlot() {
        return currentTimeSlot;
    }

    public void shift() {
        currentTimeSlot++;
        if (currentTimeSlot == slots.length + 1) {
            resetSlots();
            currentTimeSlot = 1;
        }
    }

    public void resetSlots() {
        Arrays.fill(slots, SlotStatus.UNOCCUPIED);
    }

    public String toString() {
        StringBuilder outStr = new StringBuilder("[ ");
        for (SlotStatus occupant : slots) {
            outStr.append(occupant.toString()).append(" ");
        }
        outStr.append("]");
        return outStr.toString();
    }

}
