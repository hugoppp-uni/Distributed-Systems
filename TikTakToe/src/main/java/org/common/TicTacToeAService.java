package org.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface TicTacToeAService extends Remote {
    String FIRST_MOVE_YOUR_MOVE = "your_move";
    String FIRST_MOVE_OPPONENT_MOVE = "opponent_move";
    String FIRST_MOVE_NO_OPPONENT_FOUND = "no_opponent_found";
    String KEY_GAME_ID = "Game ID";
    String KEY_OPPONENT_NAME = "Opponent Name";
    String KEY_FIRST_MOVE = "First Move";

    /**
     * @return Returns a Map with three keys: "Game ID", "Opponent Name", and "First Move".
     * <p/>
     * FirstMove is a String from: ["your_move", "opponent_move", "no_opponent_found"]
     * <p/>
     * Game ID and Opponent Name can be any reasonable Strings.
     */
    public HashMap<String, String> findGame(String clientName) throws RemoteException;

    String MAKE_MOVE_GAME_DOES_NOT_EXIST = "game_does_not_exist";
    String MAKE_MOVE_INVALID_MOVE = "invalid_move";
    String MAKE_MOVE_OPPONENT_GONE = "opponent_gone";
    String MAKE_MOVE_YOU_WIN = "you_win";
    String MAKE_MOVE_YOU_LOSE = "you_lose";

    // Returns
    //
    // Grid: 0,0 is on the top left
    //

    /**
     * @param x      X-Coordinate
     * @param y      Y-Coordinate
     * @param gameId UUID of the game
     * @return String from ["game_does_not_exist", "invalid_move", "opponent_gone", "you_win", "you_lose", "x,y"]
     */
    public String makeMove(int x, int y, String gameId) throws RemoteException;

    // Returns

    /**
     * @return List with all moves in the game with ID gameId. Each String has the pattern "name: x,y".
     */
    public ArrayList<String> fullUpdate(String gameId) throws RemoteException;
}
