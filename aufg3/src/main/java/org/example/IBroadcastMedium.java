package org.example;

public interface IBroadcastMedium {
    void send(Datagramm datagramm);
    Datagramm receive(int timeout);
}
