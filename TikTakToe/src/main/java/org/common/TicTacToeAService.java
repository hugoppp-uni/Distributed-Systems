package org.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface TicTacToeAService extends Remote {
    HashMap<String, String> findGame(String clientName) throws RemoteException;

    String makeMove(int x, int y, String gameId) throws RemoteException;

    ArrayList<String> fullUpdate(String gameId) throws RemoteException;
}
