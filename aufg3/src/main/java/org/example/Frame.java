package org.example;

import java.util.Arrays;

public class Frame {
    private final boolean[] array;

    public Frame(int size) {
        array = new boolean[size];
    }
    public synchronized boolean slotIsFree(int index){
        return !array[index];
    }

    public synchronized void setSlotOccupied(int index){
        array[index] = true;
    }

    public void resetSlots(){
        Arrays.fill(array, false);
    }

}
