package org.server;

import org.common.Move;
import org.common.TicTacToeAService;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeService implements TicTacToeAService {

    public static final long TIMEOUT_MS = 60000;

    GameState gameState = null;
    Queue<String> pendingClients;
    TTTLogger logger = TTTLogger.logger;

    @Override
    public HashMap<String, String> findGame(String clientName) throws RemoteException {
//        if(gameState.started()) return null; // TODO weitere Clients in warteschlange
        if (gameState == null) return connectPlayer(Player.A, clientName);
        return connectPlayer(Player.B, clientName);
    }

    private HashMap<String, String> connectPlayer(Player player, String clientName) {
        HashMap<String, String> triplet = new HashMap<>();
        if(player == Player.A) {
            gameState = new GameState(clientName);


            logger.log(Level.INFO,  "Player 1 "+ clientName + " waiting for opponent");
            try {
                synchronized (this) {
                    wait(TIMEOUT_MS);
                }
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "No opponent found for player 1");
                gameState = null;
                triplet.put(KEY_GAME_ID, "0");
                triplet.put(KEY_FIRST_MOVE, FIRST_MOVE_NO_OPPONENT_FOUND);
                triplet.put(KEY_OPPONENT_NAME, "");
                return triplet;
            }
        }
        else if(player == Player.B) {
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
                wait(TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            return MAKE_MOVE_OPPONENT_GONE;
        }

        if (gameState.gameEndResult.isPresent()){
            return MAKE_MOVE_YOU_LOSE;
        }
        var move = gameState.moves.get(gameState.moves.size() -1);
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
