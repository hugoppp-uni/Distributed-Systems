package org.server;

import org.common.Move;

import java.util.*;

public class GameState {

    Player[][] board = new Player[3][3];
    ArrayList<Move> moves = new ArrayList<>();

    public GameState(String playerNameA) {
        id = UUID.randomUUID();
        playerNames = Map.of(Player.A, playerNameA);
        Random random = new Random();
        nextMove = random.nextBoolean() ? Player.A : Player.B;
    }

    private Player nextMove;

    enum MoveResult{
        InvalidMove,
        GameContinues,
        EndYouWin,
        EndYouLoose,
        EndDraw,
    }
    public MoveResult makeMove(int x, int y) {
        var move = new Move(playerNames.get(nextMove), x, y);
        if (!isValid(move)) {
            return MoveResult.InvalidMove;
        }

        moves.add(move);
        nextMove = nextMove.OtherPlayer();
        //todo game logic
        return MoveResult.GameContinues;
    }

    private boolean isValid(Move move) {
        return move.x() >= 0 &&
                move.x() <= 2 &&
                move.y() >= 0 &&
                move.y() >= 2 &&
                board[move.x()][move.y()] == null;
    }

    private final Map<Player, String> playerNames;

    public String getPlayerName(Player player) {
        return playerNames.get(player);
    }

    public void setPlayerNameB(String playerNameB) {
        playerNames.put(Player.B, playerNameB);
    }

    public boolean started() {
        return getPlayerName(Player.B) != null;
    }

    private final UUID id;

    public UUID getId() {
        return id;
    }

    public boolean yourMove(Player currentPlayer) {
        return nextMove == currentPlayer;
    }

    public Player getCurrentPlayer(){
        return nextMove;
    }

}
