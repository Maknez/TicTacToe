import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TicTacToeServer {

    private     static      int [] PORTlist = new int[255];
    private     static      int listElements = 0;

    private     static      ServerSocket listener = null;

    private     static      boolean portIsCorrect = false;
    private     static      boolean portIsNotBusy = false;
    private     static      boolean setNewServerCorrect = false;

    private     static      Scanner scanner = new Scanner(System.in);

    private static void displayPORTs() {
        System.out.println("\nBusy PORTs: ");
        for (int i = 0; i < listElements; i++) {
            if (PORTlist[i] != 0){
                System.out.println(PORTlist[i]);
            }
        }
        System.out.println("");
    }

    private static void setPORT() {
        while(!portIsCorrect || !portIsNotBusy) {
            System.out.print("\nPress the port number (f. e. 1234): ");
            String portAsString = scanner.nextLine();
            validatePORTValue(portAsString);
            if(portIsCorrect) {
                int portAsInt = Integer.parseInt(portAsString);
                validatePORTlist(portAsInt);
                if(portIsNotBusy) {
                    PORTlist[listElements] = portAsInt;
                    listElements = listElements + 1;
                }
            }
            displayPORTs();
        }
        portIsCorrect = false;
        portIsNotBusy = false;
    }

    private static void validatePORTValue(String portAsString) {
        if (!Pattern.matches("[1-9][0-9]+", portAsString)) {
            portIsCorrect = false;
            System.out.println("\nIncorrect PORT value!");
        } else {
            portIsCorrect = true;
        }
    }

    private static void validatePORTlist(int portAsInt) {
        portIsNotBusy = true;
        for (int i = 0; i <= listElements; i++) {
            if (PORTlist[i] == portAsInt) {
                portIsNotBusy = false;
                System.out.println("\nThis PORT is busy!");
            }
        }
    }

    private static ServerSocket setServerSocket() {

        try {
            listener = new ServerSocket(PORTlist[listElements-1]);
        } catch (IOException e) {
            System.out.println("Setting new ServerSocket problem!");
            e.printStackTrace();
        }
        return listener;
    }

    private static ServerSocket createNewRoom (){

        setPORT();
        listener = setServerSocket();
        System.out.println("Tic Tac Toe Server is Running on PORT: " +PORTlist[listElements-1]);
        return listener;
    }

    private static void createMoreRooms() {
        while(true) {
            System.out.print("\nDo you want to create new room? y/n: ");
            String temp = scanner.nextLine();
            if (Pattern.matches("[yY]", temp)) {
                setNewServerCorrect = false;
                displayPORTs();
                break;
            }
            else if (Pattern.matches("[nN]", temp)){
                setNewServerCorrect = true;
                break;
            }
            else {
                System.out.println("Incorrect answer!");
            }
        }
    }

    public static void main(String[] args) throws Exception {

        ServerSocket listener;

        while (!setNewServerCorrect) {
            listener = createNewRoom();

            Game game = new Game();
            Game.Player playerX = game.new Player(listener.accept(), 'X');
            Game.Player playerO = game.new Player(listener.accept(), 'O');
            listener.close();

            playerX.setOpponent(playerO);
            playerO.setOpponent(playerX);
            game.currentPlayer = playerX;
            playerX.start();
            playerO.start();

            createMoreRooms();
        }
    }
}


