package org.server;

public enum Player {
    A, B;

    public Player OtherPlayer(){
        return this == Player.A ? Player.B : Player.A;
    }
}
