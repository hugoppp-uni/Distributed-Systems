package org.common;

public record Move(String playerName, int x, int y) {

    @Override
    public String toString() {
        // Each String has the pattern "name: x,y".
        return "%s: %d,%d".formatted(playerName, x, y);
    }

    public Move CreateFromString(String s) {
        //todo parse
        return new Move("", 0, 0);
    }


}
