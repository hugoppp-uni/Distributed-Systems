package org.client;
import org.common.TicTacToeAService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static org.common.TicTacToeAService.*;

public class TicTacToeGUI extends JFrame {

    private class TTTListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {

            // TODO handle first round

            var button = (TTTButton)event.getSource();
            if (hasWinner || button.marked)
                return;

            System.err.println("Button pressed");
            button.setText(marker);
            prompt.setText("Opponents move");
            try{
                String opponentMove = stub.makeMove(button.x, button.y, triplet.get(TicTacToeAService.KEY_GAME_ID));
                drawMove(opponentMove);
                prompt.setText("Your move");
            } catch (Exception e) {
                System.err.println("Exception:" + e);
                e.printStackTrace();
            }
        }
    }

    private class TTTButton extends JButton {

        public static final int BUTTON_FONT_SIZE = 100;

        public boolean marked = false;
        public int x;
        public int y;

        TTTButton(int x, int y) {
            super();
            this.x = x;
            this.y = y;
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, BUTTON_FONT_SIZE));
            addActionListener(new TTTListener());
//            setText(x + "," + y);
            setText("");
        }
    }

    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 500;
    public static final String TITLE = "TicTacToe";
    private final Container contentPane;
    private JPanel gridPanel = new JPanel();
    private JLabel prompt;
    private final TTTButton[][] board = new TTTButton[3][3];
    private boolean hasWinner = false;
    private String marker;
    private String opponent_marker;

    private final TicTacToeAService stub;
    private final HashMap<String, String> triplet;

    public TicTacToeGUI(TicTacToeAService stub, HashMap<String, String> triplet, String clientName) {
        super();
        this.stub = stub;
        this.triplet = triplet;

        marker = triplet.get(KEY_FIRST_MOVE).equals(FIRST_MOVE_YOUR_MOVE) ? "x" : "o";
        opponent_marker = marker.equals("x") ? "o" : "x";

        // pane settings
        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        prompt = new JLabel("Tic Tac Toe");

        var topPanel = new JPanel(new FlowLayout());
        topPanel.add(prompt);
        contentPane.add(topPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(3, 3));
        contentPane.add(gridPanel, BorderLayout.CENTER);

        setTitle(TITLE + " - " + clientName);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        initBoard();
    }

    private void initBoard() {
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                TTTButton button = new TTTButton(x, y);
                board[x][y] = button;
                gridPanel.add(button);
            }
        }
    }

    private void drawMove(String move) {
        switch(move) {
            // TODO
            case MAKE_MOVE_GAME_DOES_NOT_EXIST -> {}
            case MAKE_MOVE_OPPONENT_GONE -> {}
            case MAKE_MOVE_INVALID_MOVE -> {}
            case MAKE_MOVE_YOU_LOSE -> {
                hasWinner = true;
            }
            case MAKE_MOVE_YOU_WIN -> {
                hasWinner = true;
            }
            default -> {    // x,y
                int x = move.charAt(0);
                int y = move.charAt(2);
                board[x][y].setText(opponent_marker);
                board[x][y].marked = true;
            }
        }
    }

}
