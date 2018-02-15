import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TicTacToeServer {

    private     static      int [] PORTlist = new int[255];
    private     static      int [] playerNumber = new int[255];

    private     static      int listElements = 0;
    private     static      int roomNumber = 0;

    private     static      ServerSocket listener = null;

    private     static      boolean portIsCorrect = false;
    private     static      boolean portIsNotBusy = false;
    private     static      boolean setNewServerCorrect = false;

    private     static      Scanner scanner = new Scanner(System.in);

    private static void displayPORTs() {
        System.out.println("\nActive PORTs: ");
        for (int i = 0; i < listElements; i++) {
            if (PORTlist[i] != 0){
                System.out.println((i + 1) + ". Room on PORT: " + PORTlist[i] + "  -> " + playerNumber[i] + "/2 players.");
            }
        }
        System.out.println();
    }

    private static void displayPlayers(int room) {
        if (PORTlist[room] != 0){
            System.out.println((room + 1) + ". Room: " + playerNumber[room] + "/2 players.");
            if (playerNumber[room] < 2) {
                playerNumber[room]++;
            }
        }
    }

    private static void setPORT() {
        if(roomNumber != 0) {
            displayPORTs();
        }
        while(!portIsCorrect || !portIsNotBusy) {
            System.out.print("\nPress the port number (1000 - 9999): ");
            String portAsString = scanner.nextLine();
            validatePORTValue(portAsString);
            if(portIsCorrect) {
                int portAsInt = Integer.parseInt(portAsString);
                validatePORTlist(portAsInt);
                if(portIsNotBusy) {
                    listElements = 0;
                    for (int i = 0; i < 255; i++) {
                        if (PORTlist[i] == 0) {
                            PORTlist[i] = portAsInt;
                            for (int j = 0; j < 255; j++) {
                                if (PORTlist[j] != 0) {
                                    listElements++;
                                }
                            }
                            playerNumber[i] = 0;
                            roomNumber = i;
                            break;
                        }
                    }
                }
            }
            displayPORTs();
        }
        portIsCorrect = false;
        portIsNotBusy = false;
    }

    private static void validatePORTValue(String portAsString) {
        if (!Pattern.matches("[1-9][0-9][0-9][0-9]", portAsString)) {
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

    private static void createMoreRooms(Game game[]) {
        while(true) {
            System.out.print("\nDo you want to create new room? y/n: ");
            String temp = scanner.nextLine();
            if (Pattern.matches("[yY]", temp)) {

                setNewServerCorrect = false;

                for (int i = 0; i < roomNumber; i++) {
                    if (!game[i].currentPlayer.isAlive() || !game[i].currentPlayer.opponent.isAlive() || game[i].currentPlayer.PORT != PORTlist[i]) {
                        releasePORT(PORTlist[i]);
                    }
                }

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

    private static void releasePORT(int PORT) {
        for (int i = 0; i < listElements; i++) {
            if (PORT == PORTlist[i]){
                System.out.println(PORTlist[i]);
                PORTlist[i] = 0;
            }
        }
    }
    public static void main(String[] args) throws Exception {

        ServerSocket listener;


        Game []game = new Game[255];
        for( int x=0; x<255; x++ ) {
            game[x] = new Game();
            playerNumber[x] = 0;
            PORTlist[x] = 0;
        }

        while (!setNewServerCorrect) {

            listener = createNewRoom();

            displayPlayers(roomNumber);
            Game.Player playerX = game[roomNumber].new Player(listener.accept(), 'X', PORTlist[roomNumber]);
            displayPlayers(roomNumber);
            Game.Player playerO = game[roomNumber].new Player(listener.accept(), 'O', PORTlist[roomNumber]);
            displayPlayers(roomNumber);

            listener.close();

            playerX.setOpponent(playerO);
            playerO.setOpponent(playerX);
            game[roomNumber].currentPlayer = playerX;
            playerX.start();
            playerO.start();

            createMoreRooms(game);
        }
    }
}


