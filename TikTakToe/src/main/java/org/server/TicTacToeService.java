package org.server;

import org.common.Move;
import org.common.TicTacToeAService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class TicTacToeService implements TicTacToeAService {
    GameState gameStateOrNull = null;

    @Override
    public HashMap<String, String> findGame(String clientName) throws RemoteException {
        if (gameStateOrNull == null) {
            return connectPlayerA(clientName);
        }
        if (gameStateOrNull.started())
            return connectPlayerB(clientName);

        return null;//todo
    }

    private HashMap<String, String> connectPlayerA(String clientName) {
        gameStateOrNull = new GameState(clientName);
        //todo
        //     > block on condition variable
        //     > if timeout:
        //       -return [0, "", "no_opponent_found"]
        return new HashMap<>() {{
            put(KEY_GAME_ID, gameStateOrNull.getId().toString());
            put(KEY_FIRST_MOVE, gameStateOrNull.yourMove(Player.A) ?
                    FIRST_MOVE_YOUR_MOVE :
                    FIRST_MOVE_OPPONENT_MOVE);
            put(KEY_OPPONENT_NAME, gameStateOrNull.getPlayerName(Player.B));
        }};
    }

    private HashMap<String, String> connectPlayerB(String clientName) {
        gameStateOrNull.setPlayerNameB(clientName);
        //todo
        //   * notify both players and return the Triplet
        return new HashMap<>() {{
            put(KEY_GAME_ID, gameStateOrNull.getId().toString());
            put(KEY_FIRST_MOVE, gameStateOrNull.yourMove(Player.B) ?
                    FIRST_MOVE_YOUR_MOVE :
                    FIRST_MOVE_OPPONENT_MOVE);
            put(KEY_OPPONENT_NAME, gameStateOrNull.getPlayerName(Player.A));
        }};
    }

    @Override
    public String makeMove(int x, int y, String gameId) throws RemoteException {
        if (!gameExits(gameId))
            return MAKE_MOVE_GAME_DOES_NOT_EXIST;

        GameState.MoveResult moveResult = gameStateOrNull.makeMove(x, y);

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
                gameStateOrNull.moves.stream()
                        .map(Move::toString)
                        .toList());
    }

    private boolean gameExits(String gameId) {
        return gameStateOrNull != null && gameStateOrNull.getId().toString().equals(gameId);
    }
}
