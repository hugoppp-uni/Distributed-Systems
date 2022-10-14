package org.client;
import org.common.TicTacToeAService;
import org.server.TTTLogger;
import org.server.TicTacToeService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class TicTacToeGUI extends JFrame {

    private static class TTTButton extends JButton {
        public int x;
        public int y;

        TTTButton(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }
    }

    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 500;
    public static final String TITLE = "TicTacToe";
    private final Container pane;
    private final TTTButton[][] board = new TTTButton[3][3];
    private boolean hasWinner = false;

    private final TicTacToeAService stub;
    private final HashMap<String, String> triplet;

    public TicTacToeGUI(TicTacToeAService stub, HashMap<String, String> triplet) {
        super();
        this.stub = stub;
        this.triplet = triplet;

        // pane settings
        pane = getContentPane();
        pane.setLayout(new GridLayout(3,3));
        setTitle(TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        initBoard();
    }

    private class TTTListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if(((TTTButton)event.getSource()).getText().equals("") && !hasWinner) {
                var source = (TTTButton)event.getSource();
                try{
                    stub.makeMove(source.x, source.y, triplet.get(TicTacToeAService.KEY_GAME_ID));
                    updateBoard(stub.fullUpdate(triplet.get(TicTacToeAService.KEY_GAME_ID)));
                } catch (Exception e) {
                    TTTLogger.logger.log(Level.WARNING, "Exception " + e);
                }
            }
        }
    }

    private void initBoard() {
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                TTTButton button = new TTTButton(x, y);
//                button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 100));
                button.addActionListener(new TTTListener());
                board[x][y] = button;
                pane.add(button);
            }
        }
    }

    private void updateBoard(ArrayList<String> fullUpdate) {
        for (var s: fullUpdate) {
            System.err.println(s);
        }
    }
}
