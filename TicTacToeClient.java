import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.swing.*;

public class TicTacToeClient {

    private     JFrame frame = new JFrame("Tic Tac Toe");
    private     JLabel messageLabel = new JLabel("");
    private     ImageIcon icon;
    private     ImageIcon opponentIcon;

    private     Square[] board = new Square[9];
    private     Square currentSquare;

    private     static      int PORT;
    private     Socket socket;
    private     BufferedReader in;
    private     PrintWriter out;

    private     static      boolean portIsCorrect = false;

    private     static Scanner scanner = new Scanner(System.in);

    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);

        Square() {
            setBackground(Color.white);
            add(label);
        }

        void setIcon(Icon icon) {
            label.setIcon(icon);
        }
    }

    // Constructs the client by connecting to a server, laying out the GUI and registering GUI listeners.
    private TicTacToeClient(String serverAddress) throws Exception {
        // Setup networking
        boolean connectionIsCorrect = false;

        while(!connectionIsCorrect) {
            try {
                socket = new Socket(serverAddress, PORT);
                connectionIsCorrect = true;
                System.out.println("Connected to the server!");
            } catch (Exception e) {
                System.out.println("Connection problem! \nPORT number is incorrect or the room is full!");
                setPORT();
            }
        }
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Layout GUI
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, "South");
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 1, 1));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);}});
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    //tutki
    //http://cs.lmu.edu/~ray/notes/javanetexamples/#tictactoe
    //https://www.youtube.com/watch?v=aIaFFPatJjY&t=2440s
   //* The main thread of the client will listen for messages from the server.  
   //The first message will be a "WELCOME" message in which we receive our mark.
   //Then we go into a loop listening for:
   //--> "VALID_MOVE", --> "OPPONENT_MOVED", --> "VICTORY", --> "DEFEAT", --> "TIE", --> "OPPONENT_QUIT, --> "MESSAGE" messages, and handling each message appropriately.
   //The "VICTORY","DEFEAT" and "TIE" ask the user whether or not to play another game. 
   //If the answer is no, the loop is exited and the server is sent a "QUIT" message.  If an OPPONENT_QUIT message is recevied then the loop will exit and the server will be sent a "QUIT" message also.
   private void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icon = new ImageIcon(mark == 'X' ? "C:/Users/maknez/Desktop/TicTacToe/src/x.gif" : "C:/Users/maknez/Desktop/TicTacToe/src/o.gif");
                opponentIcon  = new ImageIcon(mark == 'X' ? "C:/Users/maknez/Desktop/TicTacToe/src/o1.gif" : "C:/Users/maknez/Desktop/TicTacToe/src/x1.gif");
                frame.setTitle("Tic Tac Toe - Player " + mark);

            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You WON :)");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You LOST :(");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied :|");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
                else if (response.startsWith("ERROR")) {
                    messageLabel.setText(response.substring(6));
                    break;
                }
            }
            out.println("QUIT");
        }
        finally {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
            "Do you want to play again?",
            "Play again",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    private static void setPORT() {

        String portAsString = "";
        while(!portIsCorrect) {
            System.out.print("\nPress the port number (1000-9999): ");
            portAsString = scanner.nextLine();
            validatePORTValue(portAsString);
        }
        PORT = Integer.parseInt(portAsString);
        portIsCorrect = false;
    }

    private static void validatePORTValue(String portAsString) {
        if (!Pattern.matches("[1-9][0-9][0-9][0-9]", portAsString)) {
            portIsCorrect = false;
            System.out.println("\nIncorrect PORT value!");
        } else {
            portIsCorrect = true;
        }
    }

    public static void main(String[] args) {

        try{
            while (true) {
                setPORT();
                String serverAddress = TicTacToeServerConfig.getHostName();
                TicTacToeClient client = new TicTacToeClient(serverAddress);
                client.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                client.frame.setSize(240, 240);
                client.frame.setVisible(true);
                client.frame.setResizable(false);
                client.play();

                if (!client.wantsToPlayAgain()) {
                    break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Connection LOST!");
            System.exit(1);
        }
    }
}