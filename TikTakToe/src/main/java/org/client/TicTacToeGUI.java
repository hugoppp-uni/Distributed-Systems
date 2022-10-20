package org.client;

import org.common.TicTacToeAService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.HashMap;

public class TicTacToeGUI extends JFrame {

  private class TTTButton extends JButton {
    public int x;
    public int y;

    TTTButton(String text, int x, int y) {
      super(text);
      this.x = x;
      this.y = y;
    }
  }

  JFrame frame = new JFrame();
  JPanel t_panel = new JPanel();
  JPanel bt_panel = new JPanel();
  JLabel textfield = new JLabel();
  TTTButton[][] bton = new TTTButton[3][3];

  TicTacToeGUI(TicTacToeAService stub, String clientName) throws RemoteException {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 800);
    frame.getContentPane().setBackground(new Color(50, 50, 50));
    frame.setTitle("Tic Tac Toe");
    frame.setLayout(new BorderLayout());
    frame.setVisible(true);

    textfield.setBackground(new Color(120, 20, 124));
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
        bton[x][y] = new TTTButton("", x, y);
        bt_panel.add(bton[x][y]);
        bton[x][y].setFont(new Font("Ink Free", Font.BOLD, 120));
        bton[x][y].setFocusable(false);
        bton[x][y].addActionListener(this::buttonPressed);
      }
    }

    t_panel.add(textfield);
    frame.add(t_panel, BorderLayout.NORTH);
    frame.add(bt_panel);

    textfield.setText("Connecting");
    HashMap<String, String> connectResponse = connect(stub, clientName);
    textfield.setText(connectResponse.get(TicTacToeAService.KEY_FIRST_MOVE));
  }

  private HashMap<String, String> connect(TicTacToeAService stub, String clientName) throws RemoteException {
    HashMap<String, String> response = stub.findGame(clientName);
    System.err.println("Game started: " + response);
    return response;
  }

  private void buttonPressed(ActionEvent e) {
    TTTButton button = ((TTTButton) e.getSource());
    System.out.println(button.x + " " + button.y);
  }

  /*
  private void drawMove() {
    switch (move) {
      // TODO
      case MAKE_MOVE_GAME_DOES_NOT_EXIST -> {
      }
      case MAKE_MOVE_OPPONENT_GONE -> {
      }
      case MAKE_MOVE_INVALID_MOVE -> {
      }
      case MAKE_MOVE_YOU_LOSE -> {
        hasWinner = true;
      }
      case MAKE_MOVE_YOU_WIN -> {
        hasWinner = true;
      }
      default -> {    // x,y
        if (button != null) button.setText(marker);
        int x = move.charAt(0) - '0';
        int y = move.charAt(2) - '0';
        board[x][y].setText(opponent_marker);
        board[x][y].marked = true;
      }
    }
  }
   */

}
