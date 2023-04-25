import java.security.KeyPair;
import java.util.Scanner;

public class MainClient {
    private static final Scanner in = new Scanner(System.in);
    public static void main ( String[] args ) throws Exception {
        boolean mainAlive = true;
        while (mainAlive){
            boolean subAlive = true;
            System.out.println("\n*************** Welcome to GROUP 11 ******************************\n");
            System.out.println("1. Create your credentials");
            System.out.println("2. Quit");
            int input = in.nextInt();
            switch(input){
                case 1:
                    Client client = new Client ( 8000 );
                    System.out.println("\nInsert your username");
                    String client_name = in.next();
                    client.setClientName(client_name);
                    client.initClient();
                    client.execute(); //HANDSHAKE
                    while (subAlive){
                        System.out.println("\n*************** USER ACTIONS *******************");
                        System.out.println("\n1. Request your file");
                        System.out.println("2. Change accounts");
                        int req = in.nextInt();
                        switch (req){
                            case 1:
                                client.request();
                                break;
                            case 2:
                                subAlive = false;
                                break;
                        }
                    }
                    break;
                case 2:

            }
        }

    }

}
