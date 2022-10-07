package org.client;

import org.common.TicTacToeAService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;


public class Client {

    //https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/hello/hello-world.html
    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            TicTacToeAService stub = (TicTacToeAService) registry.lookup("TickTacToeAService");

            HashMap<String, String> response = stub.findGame("test");
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}