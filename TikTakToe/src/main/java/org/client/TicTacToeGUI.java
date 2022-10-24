package org.client;

import org.common.Move;
import org.common.TicTacToeAService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.common.TicTacToeAService.*;

public class TicTacToeGUI extends JFrame {

  private final TicTacToeAService stub;
  private final String clientName;

  private static class TTTButton extends JButton {
    public int x;
    public int y;
    private boolean marked = false;
    String lastMarker = null;

    TTTButton(String text, int x, int y) {
      super(text);
      this.x = x;
      this.y = y;
    }

    public void mark(String marker) {

      lastMarker = getText();
      setText(marker);
      marked = true;
    }

    public void unmark() {
      setText(lastMarker);
      marked = false;
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
  boolean myMove;

  TicTacToeGUI(TicTacToeAService stub, String clientName) throws RemoteException, InterruptedException {

    this.stub = stub;
    this.clientName = clientName;


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

    textfield.setText("Waiting for player");
    HashMap<String, String> connectResponse = connect(stub, clientName);
    gameId = connectResponse.get(KEY_GAME_ID);

    if (connectResponse.get(KEY_FIRST_MOVE).equals(FIRST_MOVE_NO_OPPONENT_FOUND))
      JOptionPane.showMessageDialog(null, "No opponent found");

    myMove = connectResponse.get(KEY_FIRST_MOVE).equals(FIRST_MOVE_YOUR_MOVE);
    myMarker = myMove ? "x" : "o";
    opponentMarker = myMove ? "o" : "x";
    updateCurrentMoveTo(myMove ? CurrentMove.My : CurrentMove.Opponent);

    if (connectResponse.get(KEY_FIRST_MOVE).isEmpty()) {

      List<Move> moves = stub.fullUpdate(gameId)
                                  .stream()
                                  .map(Move::createFromFullUpdateString)
                                  .collect(Collectors.toList());
      for (Move move : moves) {
        markMove(move);
      }
      Move lastMove = moves.get(moves.size() - 1);
      boolean myMove = !lastMove.playerName().equals(clientName);
      updateCurrentMoveTo(myMove ? CurrentMove.My : CurrentMove.Opponent);
      if (!myMove)
        pollForOpponentMove(stub, moves.size());
    }


    if (connectResponse.get(KEY_FIRST_MOVE).equals(FIRST_MOVE_OPPONENT_MOVE)) {
      pollForOpponentMove(stub, 0);
    }
  }

  private void pollForOpponentMove(TicTacToeAService stub, int playedMoves) throws InterruptedException,
    RemoteException {
    while (true) {
      Thread.sleep(500);
      ArrayList<String> strings = stub.fullUpdate(gameId);
      if (strings.size() > playedMoves) {
        markMove(Move.createFromFullUpdateString(strings.get(playedMoves)));
        updateCurrentMoveTo(CurrentMove.My);
        break;
      }
    }
  }

  private HashMap<String, String> connect(TicTacToeAService stub, String clientName) throws RemoteException {
    HashMap<String, String> response = stub.findGame(clientName);
    System.err.println("Game started: " + response);
    return response;
  }

  private void buttonPressed(ActionEvent e) {
    if (!myMove) return;
    updateCurrentMoveTo(CurrentMove.Opponent);

    TTTButton button = ((TTTButton) e.getSource());
    System.out.println(button.x + " " + button.y);

    button.mark(myMarker);
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
        JOptionPane.showMessageDialog(null, "Game does not exist");
      }
      case MAKE_MOVE_OPPONENT_GONE -> {
        JOptionPane.showMessageDialog(null, "Opponent gone");
        textfield.setText("Opponent gone");
      }
      case MAKE_MOVE_INVALID_MOVE -> {
        JOptionPane.showMessageDialog(null, "Invalid move");
        board[myMoveX][myMoveY].unmark();
        updateCurrentMoveTo(CurrentMove.My);
      }
      case MAKE_MOVE_YOU_LOSE -> {
        textfield.setText("You lose");
      }
      case MAKE_MOVE_YOU_WIN -> {
        textfield.setText("You win");
      }
      default -> {
        // x,y
        markMove(move.charAt(0) - '0', move.charAt(2) - '0', opponentMarker);
        updateCurrentMoveTo(CurrentMove.My);
      }
    }
  }

  enum CurrentMove {
    My,
    Opponent
  }
  public void updateCurrentMoveTo(CurrentMove move){
    if (move == CurrentMove.My){
      myMove = true;
      textfield.setText("Your move");
    } else{
      myMove = false;
      textfield.setText("Opponent move");
    }

  }

  private void markMove(Move move) {
    markMove(move.x(), move.y(), Objects.equals(move.playerName(), clientName) ? myMarker : opponentMarker);
  }

  private void markMove(int x, int y, String marker) {
    SwingUtilities.invokeLater(() -> board[x][y].mark(marker));
  }

}
