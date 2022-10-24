package org.common;

public record Move(String playerName, int x, int y) {
    @Override
    public String toString() {
        // Each String has the pattern "name: x,y".
        return "%s: %d,%d".formatted(playerName, x, y);
    }

    public static Move createFromFullUpdateString(String s) {
        //todo parse
        String[] split = s.split(":");
        int x = split[1].charAt(1) - '0';
        int y = split[1].charAt(3) - '0';
        return new Move(split[0], x, y);
    }


}
