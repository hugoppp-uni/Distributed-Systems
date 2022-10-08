package org.server;

import org.common.Move;

import java.util.*;

public class GameState {

    public static final int BOARD_SIZE = 3;
    Player[][] board = new Player[BOARD_SIZE][BOARD_SIZE];
    ArrayList<Move> moves = new ArrayList<>();

    public GameState(String playerNameA) {
        id = UUID.randomUUID();
        playerNames.put(Player.A, playerNameA);
        Random random = new Random();
        currentPlayer = random.nextBoolean() ? Player.A : Player.B;
    }

    private Player currentPlayer;

    public enum MoveResult {
        InvalidMove,
        GameContinues,
        EndYouWin,
        EndYouLoose,
        EndDraw,
    }

    public MoveResult makeMove(int x, int y) {
        var move = new Move(playerNames.get(currentPlayer), x, y);
        if (!isValid(move)) {
            return MoveResult.InvalidMove;
        }

        moves.add(move);
        board[x][y] = currentPlayer;
        Optional<MoveResult> gameEndResult = checkWinningCondition();
        currentPlayer = currentPlayer.OtherPlayer();
        return gameEndResult.orElse(MoveResult.GameContinues);
    }

    private Optional<MoveResult> checkWinningCondition() {

        Optional<Player> winner = checkVerticalWinCondition()
                .or(this::checkHorizontalWinCondition)
                .or(this::checkDiagonalWinCondition);

        if (winner.isPresent())
            return Optional.of(getCurrentPlayer() == winner.get() ? MoveResult.EndYouWin : MoveResult.EndYouLoose);

        if (checkDraw())
            return Optional.of(MoveResult.EndDraw);

        return Optional.empty();
    }

    private boolean checkDraw() {
        return moves.size() == BOARD_SIZE * BOARD_SIZE;
    }

    private Optional<Player> checkVerticalWinCondition() {
        for (int x = 0; x < BOARD_SIZE; x++) {
            Player playerAtFirstPos = board[x][0];
            if (playerAtFirstPos == null)
                continue;

            for (int y = 1; board[x][y] == playerAtFirstPos; y++) {
                if (y == BOARD_SIZE - 1)
                    return Optional.of(playerAtFirstPos);
            }
        }
        return Optional.empty();
    }

    private Optional<Player> checkHorizontalWinCondition() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            Player playerAtFirstPos = board[0][y];
            if (playerAtFirstPos == null)
                continue;

            for (int x = 1; board[x][y] == playerAtFirstPos; x++) {
                if (x == BOARD_SIZE - 1)
                    return Optional.of(playerAtFirstPos);
            }
        }
        return Optional.empty();
    }

    private Optional<Player> checkDiagonalWinCondition() {
        Player playerAtMiddle = board[BOARD_SIZE / 2][BOARD_SIZE / 2];
        if (playerAtMiddle == null)
            return Optional.empty();

        {
            int x = 0;
            int y = 0;
            while (board[x][y] == playerAtMiddle) {
                if (x == BOARD_SIZE - 1)
                    return Optional.of(playerAtMiddle);
                x++;
                y++;
            }
        }
        {
            int x = 0;
            int y = BOARD_SIZE - 1;
            while (board[x][y] == playerAtMiddle) {
                if (x == BOARD_SIZE - 1)
                    return Optional.of(playerAtMiddle);
                x++;
                y--;
            }
        }

        return Optional.empty();
    }

    private boolean isValid(Move move) {
        return move.x() >= 0 &&
                move.x() < BOARD_SIZE &&
                move.y() >= 0 &&
                move.y() < BOARD_SIZE &&
                board[move.x()][move.y()] == null;
    }

    private HashMap<Player, String> playerNames = new HashMap<>();

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
        return this.currentPlayer == currentPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

}
