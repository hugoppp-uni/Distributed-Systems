package org.server;

import org.common.TicTacToeAService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.logging.Level;

public class Server {

    // TODO fehlersemantik auf spielebene

    public static final int PORT = 1099;
    //https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/hello/hello-world.html
    public static void main(String[] args) {
        TTTLogger.init("server.log");

        try {
            var ticTacToeService = new TicTacToeService();
            TicTacToeAService stub = (TicTacToeAService) UnicastRemoteObject.exportObject(ticTacToeService, PORT);

            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.bind("TicTacToeAService", stub);
            System.err.println("Server ready");
            TTTLogger.logger.log(Level.INFO, "Server ready");
        } catch (Exception e) {
            TTTLogger.logger.log(Level.WARNING, "Server exception: " + e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}
