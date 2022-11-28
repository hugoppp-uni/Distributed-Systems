package org.example;

public enum StationClass {
    A, B;

    public byte toByte() {
        return (byte) this.toString().charAt(0);
    }
}
