import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Game {
    // a board of 9 squares
    private Player[] board = {
            null, null, null,
            null, null, null,
            null, null, null};
    //current player
    Player currentPlayer;
    private boolean connectionError = false;
    // winner

    private boolean hasWinner() {
        return
            (board[0] != null && board[0] == board[1] && board[0] == board[2])
            ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
            ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
            ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
            ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
            ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
            ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
            ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }
    // no empty squares
    private boolean boardFilledUp() {
        for (Player aBoard : board) {
            if (aBoard == null) {
                return false;
            }
        }
        return true;
    }
    // thread when player tries a move
    private synchronized boolean legalMove(int location, Player player) {
        if (player == currentPlayer && board[location] == null) {
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
            return true;
        }
        return false;
    }

    class Player extends Thread {

        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        // thread handler to initialize stream fields
        Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                output.println("ERROR opponent disconnected!");
                System.out.println("\n!!!Connection with player: " + mark + " on PORT: " + socket.getLocalPort() + " LOST!!!");
            }
        }
        //Accepts notification of who the opponent is.
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
        //Handles the otherPlayerMoved message.
        private void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(
                    hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
        }

        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected");

                // Tell the first player that it is his/her turn.
                if (mark == 'X') {
                    output.println("MESSAGE Your move");
                }

                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (legalMove(location, this)) {
                            output.println("VALID_MOVE");
                            output.println(hasWinner() ? "VICTORY" : boardFilledUp() ? "TIE" : "");
                        } else {
                            if(!connectionError) {
                                output.println("MESSAGE ...");
                            }
                            else {
                                output.println("ERROR opponent disconnected!");
                            }
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                connectionError = true;
                System.out.println("\n!!!Connection with player: " + mark + " on PORT: " + socket.getLocalPort() + " LOST!!!");
                System.out.print("\nDo you want to create new room? y/n: ");

            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Closing socket problem!");
                }
            }
        }
    }
}