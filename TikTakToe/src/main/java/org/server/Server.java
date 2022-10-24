package org.server;

import org.common.TicTacToeAService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.logging.Level;

public class Server {

    // TODO fehlersemantik auf spielebene

    public static int port;
    public static String address;
    //https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/hello/hello-world.html
    public static void main(String[] args) {
        if(args.length < 2) TTTLogger.logger.log(Level.WARNING, "Invalid args");
        port = Integer.parseInt(args[0]);
        address = args[1];


        TTTLogger.init("server.log");

        try {
            var ticTacToeService = new TicTacToeService();
            TicTacToeAService stub = (TicTacToeAService) UnicastRemoteObject.exportObject(ticTacToeService, port);

            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("TicTacToeAService", stub);
            TTTLogger.logger.log(Level.INFO, "Server ready");
        } catch (Exception e) {
            TTTLogger.logger.log(Level.WARNING, "Server exception: " + e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}
