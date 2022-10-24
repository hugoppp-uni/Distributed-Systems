package org.server;

import org.common.Move;
import org.common.TicTacToeAService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public class TicTacToeService implements TicTacToeAService {

    public static final long TIMEOUT_MAKEMOVE_MS = 60_000;
    public static final long TIMEOUT_FINDGAME = 60_000;

    GameState gameState = null;
    TTTLogger logger = TTTLogger.logger;

    private final static int MAX_CLIENTS = 2;
    private final Semaphore sem = new Semaphore(MAX_CLIENTS, true);

    @Override
    public HashMap<String, String> findGame(String clientName) throws RemoteException {

        if (gameState != null && gameState.started() && (clientName.equals(gameState.getPlayerName(Player.A)) ||
                                                         clientName.equals(gameState.getPlayerName(Player.B)))) {
          return reconnectPlayer();
        }

        try {
            sem.acquire();
        } catch (InterruptedException ignored) {
        }
        synchronized (this) {
            if (gameState == null) return connectPlayer(Player.A, clientName);
            return connectPlayer(Player.B, clientName);
        }
    }

  private HashMap<String, String> reconnectPlayer() {
    HashMap<String, String> triplet = new HashMap<>();
    triplet.put(KEY_GAME_ID, gameState.getId().toString());
    triplet.put(KEY_FIRST_MOVE, "");
    triplet.put(KEY_OPPONENT_NAME, gameState.getPlayerName(gameState.getCurrentPlayer().OtherPlayer()));
    return triplet;
  }

  private HashMap<String, String> connectPlayer(Player player, String clientName) {
        HashMap<String, String> triplet = new HashMap<>();
        if (player == Player.A) {
            gameState = new GameState(clientName);


            logger.log(Level.INFO, "Player 1 " + clientName + " waiting for opponent");
            try {
                synchronized (this) {
                    var start = System.currentTimeMillis();
                    wait(TIMEOUT_FINDGAME);
                    if (System.currentTimeMillis() - start >= TIMEOUT_FINDGAME) {
                        logger.log(Level.WARNING, "No opponent found for player 1");
                        gameState = null;
                        triplet.put(KEY_GAME_ID, "0");
                        triplet.put(KEY_FIRST_MOVE, FIRST_MOVE_NO_OPPONENT_FOUND);
                        triplet.put(KEY_OPPONENT_NAME, "");
                        return triplet;
                    }

                }
            } catch (InterruptedException ignored) {
            }
        } else if (player == Player.B) {
            gameState.setPlayerNameB(clientName);
            synchronized (this) {
                this.notifyAll();
            }
        }
        triplet.put(KEY_GAME_ID, gameState.getId().toString());
        triplet.put(KEY_FIRST_MOVE, gameState.yourMove(player) ? FIRST_MOVE_YOUR_MOVE : FIRST_MOVE_OPPONENT_MOVE);
        triplet.put(KEY_OPPONENT_NAME, gameState.getPlayerName(player.OtherPlayer()));
        logger.log(Level.INFO, "Connected Player: " + clientName);
        return triplet;
    }

    @Override
    public String makeMove(int x, int y, String gameId) throws RemoteException {
        if (!gameExits(gameId))
            return MAKE_MOVE_GAME_DOES_NOT_EXIST;

        GameState.MoveResult moveResult = gameState.makeMove(x, y);
        logger.log(Level.INFO, gameState.moves.get(gameState.moves.size() - 1).toString() + " - " + moveResult.toString());

        if (GameState.MoveResult.InvalidMove == moveResult) {
            return MAKE_MOVE_INVALID_MOVE;
        }
        if (GameState.MoveResult.EndYouWin == moveResult) {
            synchronized (this) {
                this.notifyAll();
            }
            return MAKE_MOVE_YOU_WIN;
        }
        if (GameState.MoveResult.EndDraw == moveResult) {
            synchronized (this) {
                this.notifyAll();
            }
            return MAKE_MOVE_YOU_LOSE;
        }

        try {
            synchronized (this) {
                this.notifyAll();
                long start = System.currentTimeMillis();
                wait(TIMEOUT_MAKEMOVE_MS);
                if (System.currentTimeMillis() - start >= TIMEOUT_MAKEMOVE_MS) {
                    notifyAll();
                    gameState = null;
                    sem.release(MAX_CLIENTS);
                    return MAKE_MOVE_OPPONENT_GONE;
                }

            }
        } catch (InterruptedException e) {
            gameState = null;
            logger.log(Level.WARNING, "Opponent gone");
            return MAKE_MOVE_OPPONENT_GONE;
        }

        if (gameState.gameEndResult.isPresent()) {
            gameState = null;
            sem.release(MAX_CLIENTS);
            return MAKE_MOVE_YOU_LOSE;
        }
        var move = gameState.moves.get(gameState.moves.size() - 1);
        return move.x() + "," + move.y();
    }

    // Returns a list with all moves in the game with ID gameId.
    // Each String has the pattern "name: x,y".
    @Override
    public ArrayList<String> fullUpdate(String gameId) throws RemoteException {
        if (!gameExits(gameId)) {
            return null;//todo not specified?
        }

        return new ArrayList<>(
                gameState.moves.stream()
                        .map(Move::toString)
                        .toList());
    }

    private boolean gameExits(String gameId) {
        return gameState != null && gameState.getId().toString().equals(gameId);
    }
}
