import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainClient {
    private static final Scanner in = new Scanner(System.in);
    private static List<String> c_names = new ArrayList<>();
    public static void main ( String[] args ) throws Exception {
        boolean mainAlive = true;
        while (mainAlive){
            boolean subAlive = true;
            System.out.println("\n*************** Welcome to GROUP 11 ******************************\n");
            System.out.println("1. Start session - New clients");
            System.out.println("2. Quit");
            int input = in.nextInt();
            switch(input){
                case 1:
                    Client client = new Client ( 8000 ); //Handshake é feito quando o cliente é criado
                    System.out.println("\nInsert your username");
                    String client_name = in.next();
                    client.setClientName(client_name);

                    while (subAlive == true){
                        if(client.getRequestLimit() == 5){ //Sessão chegou ao limite?
                            System.out.println("\nYou have reached max requests. You will be taken to the session restart.");
                            RequestUtils.resetRequestCounter(client.getClientName());
                            break;
                        }
                        System.out.println("\n*************** USER : "+client.getClientName()+" *******************");
                        System.out.println("\n"+client.getRequestLimit()+"\n");
                        System.out.println("\n1. Request file");
                        System.out.println("2. End session");
                        int req = in.nextInt();
                        switch (req){
                            case 1:
                                client.execute();
                                break;
                            case 2:
                                subAlive = false;
                                break;
                        }
                    }
                    break;

                case 2:
                    mainAlive = false;
                    break;
            }
        }

    }

}
