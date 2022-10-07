package org.server;

import org.common.TicTacToeAService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    //https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/hello/hello-world.html
    public static void main(String args[]) {

        try {
            var ticTacToeService = new TicTacToeService();
            TicTacToeAService stub = (TicTacToeAService) UnicastRemoteObject.exportObject(ticTacToeService, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("TicTacToeAService", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
