package org.example;

import java.util.Arrays;
import java.util.Random;

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

    public Frame() {
        slots = new SlotStatus[SLOT_COUNT];
        resetSlots();
    }

    public synchronized boolean slotAvailable() {
        return Arrays.stream(slots).anyMatch(x -> x == SlotStatus.UNOCCUPIED);
    }

    public synchronized int getRandomFreeSlot() {
        if(!slotAvailable()) return -1;
        Random random = new Random();
        while(true) {
            int slotIndex = random.nextInt(SLOT_COUNT);
            if(slotIsFree(slotIndex)) return slotIndex;
        }
    }

    public synchronized boolean slotIsFree(int slotIndex) {
        return slots[slotIndex] == SlotStatus.UNOCCUPIED;
    }

    public synchronized boolean checkCollision(int slotIndex) {
        return slots[slotIndex] == SlotStatus.COLLISION;
    }

    public synchronized void setSlotOccupied(int slotIndex, StationClass stationClass) {
        SlotStatus occupant = stationClass == StationClass.A ? SlotStatus.OCCUPIED_A : SlotStatus.OCCUPIED_B;
        slots[slotIndex] = slots[slotIndex] != SlotStatus.UNOCCUPIED ? SlotStatus.COLLISION : occupant;
    }

    public synchronized void setSlotUnoccupied(int slotIndex) {
        slots[slotIndex] = SlotStatus.UNOCCUPIED;
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
