package org.client;

import org.common.TicTacToeAService;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;


public class Client {

    public static TicTacToeAService stub;

    //https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/hello/hello-world.html
    public static void main(String[] args) {
        String clientName = (args.length < 2) ? null : args[0];
        String host = (args.length < 2) ? null : args[1];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            stub = (TicTacToeAService) registry.lookup("TicTacToeAService");


            // start GUI
            new TicTacToeGUI(stub, clientName);


        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }
}