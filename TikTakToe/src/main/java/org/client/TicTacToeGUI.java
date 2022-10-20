package org.client;

import org.common.TicTacToeAService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.common.TicTacToeAService.*;

public class TicTacToeGUI extends JFrame {

  private final TicTacToeAService stub;

  private class TTTButton extends JButton {
    public int x;
    public int y;
    private boolean marked = false;

    TTTButton(String text, int x, int y) {
      super(text);
      this.x = x;
      this.y = y;
    }

    public void mark(String marker) {
      setText(marker);
      marked = true;
    }

    public boolean isMarked() {
      return marked;
    }
  }

  JFrame frame = new JFrame();
  JPanel t_panel = new JPanel();
  JPanel bt_panel = new JPanel();
  JLabel textfield = new JLabel();
  TTTButton[][] board = new TTTButton[3][3];

  String gameId;

  String myMarker;
  final String opponentMarker;

  TicTacToeGUI(TicTacToeAService stub, String clientName) throws RemoteException, InterruptedException {

    this.stub = stub;


    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 800);
    frame.getContentPane().setBackground(new Color(50, 50, 50));
    frame.setTitle("Tic Tac Toe");
    frame.setLayout(new BorderLayout());
    frame.setVisible(true);

    textfield.setBackground(new Color(0, 0, 255));
    textfield.setForeground(new Color(250, 255, 0));
    textfield.setFont(new Font("Ink Free", Font.BOLD, 75));
    textfield.setHorizontalAlignment(JLabel.CENTER);
    textfield.setText("Tic Tac Toe");
    textfield.setOpaque(true);
    t_panel.setLayout(new BorderLayout());
    t_panel.setBounds(0, 0, 800, 100);
    bt_panel.setLayout(new GridLayout(3, 3));
    bt_panel.setBackground(new Color(150, 150, 150));
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        board[x][y] = new TTTButton("", x, y);
        bt_panel.add(board[x][y]);
        board[x][y].setFont(new Font("Ink Free", Font.BOLD, 120));
        board[x][y].setFocusable(false);
        board[x][y].addActionListener(this::buttonPressed);
      }
    }

    t_panel.add(textfield);
    frame.add(t_panel, BorderLayout.NORTH);
    frame.add(bt_panel);

    textfield.setText("Connecting");
    HashMap<String, String> connectResponse = connect(stub, clientName);
    gameId = connectResponse.get(TicTacToeAService.KEY_GAME_ID);

    myMarker =
      connectResponse.get(TicTacToeAService.KEY_FIRST_MOVE) == TicTacToeAService.FIRST_MOVE_YOUR_MOVE ? "x" : "o";
    opponentMarker = myMarker.equals("x") ? "o" : "x";

    textfield.setText(connectResponse.get(TicTacToeAService.KEY_FIRST_MOVE));
    if (connectResponse.get(TicTacToeAService.KEY_FIRST_MOVE).equals(TicTacToeAService.FIRST_MOVE_OPPONENT_MOVE)) {

      while (true) {
        Thread.sleep(500);
        ArrayList<String> strings = stub.fullUpdate(gameId);
        if (strings.size() > 0) {
          handleMakeMoveResponse(strings.get(0).substring(strings.get(0).indexOf(":") + 2), -1, -1);
          break;
        }
      }


    }
  }

  private HashMap<String, String> connect(TicTacToeAService stub, String clientName) throws RemoteException {
    HashMap<String, String> response = stub.findGame(clientName);
    System.err.println("Game started: " + response);
    return response;
  }

  private void buttonPressed(ActionEvent e) {
    TTTButton button = ((TTTButton) e.getSource());
    System.out.println(button.x + " " + button.y);
    new Thread(() -> {
      try {
        handleMakeMoveResponse(stub.makeMove(button.x, button.y, gameId), button.x, button.y);
      } catch (RemoteException ex) {
        throw new RuntimeException(ex);
      }
    }).start();
  }

  private void handleMakeMoveResponse(String move, int myMoveX, int myMoveY) {
    switch (move) {
      // TODO
      case MAKE_MOVE_GAME_DOES_NOT_EXIST -> {
      }
      case MAKE_MOVE_OPPONENT_GONE -> {
      }
      case MAKE_MOVE_INVALID_MOVE -> {
      }
      case MAKE_MOVE_YOU_LOSE -> {
      }
      case MAKE_MOVE_YOU_WIN -> {
      }
      default -> {
        // x,y
        int x = move.charAt(0) - '0';
        int y = move.charAt(2) - '0';
        SwingUtilities.invokeLater(() -> {
          board[x][y].mark(opponentMarker);
          if (myMoveX >= 0) board[myMoveX][myMoveY].mark(myMarker);
        });
      }
    }
  }

}
