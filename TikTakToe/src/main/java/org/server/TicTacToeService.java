package org.server;

import org.common.Move;
import org.common.TicTacToeAService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class TicTacToeService implements TicTacToeAService {
    GameState gameState = null;

    @Override
    public HashMap<String, String> findGame(String clientName) throws RemoteException {
//        if(gameState.started()) return null; // TODO weitere Clients in warteschlange
        if (gameState == null) return connectPlayer(Player.A, clientName);
        return connectPlayer(Player.B, clientName);
    }

    private HashMap<String, String> connectPlayer(Player player, String clientName) {
        if(player == Player.A) {
            //todo
            //     > block on condition variable
            //     > if timeout:
            //       -return [0, "", "no_opponent_found"]
            gameState = new GameState(clientName);
        }
        if(player == Player.B) {
            //todo
            //   * notify both players and return the Triplet
            gameState.setPlayerNameB(clientName);
        }
        HashMap<String, String> triplet = new HashMap<>();
        triplet.put(KEY_GAME_ID, gameState.getId().toString());
        triplet.put(KEY_FIRST_MOVE, gameState.yourMove(player) ? FIRST_MOVE_YOUR_MOVE : FIRST_MOVE_OPPONENT_MOVE);
        triplet.put(KEY_OPPONENT_NAME, gameState.getPlayerName(player));
        System.err.println("Connected Player: " + clientName);
        return triplet;
    }

    @Override
    public String makeMove(int x, int y, String gameId) throws RemoteException {
        if (!gameExits(gameId))
            return MAKE_MOVE_GAME_DOES_NOT_EXIST;

        GameState.MoveResult moveResult = gameState.makeMove(x, y);

        if (GameState.MoveResult.InvalidMove == moveResult)
            return MAKE_MOVE_INVALID_MOVE;
        if (GameState.MoveResult.EndYouWin == moveResult)
            return MAKE_MOVE_YOU_WIN;
        if (GameState.MoveResult.EndYouLoose == moveResult)
            return MAKE_MOVE_YOU_LOSE;
        //todo notify other player
        //     > signal condition variable
        //     > block on condition variable
        //     > if timeout:
        //       -return "opponent_gone"
        //     > if opponent makes a move:
        //       -return "x,y"
        //   * if move ends game:
        //     > return "you_win" | "you_lose"
        return null;
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
