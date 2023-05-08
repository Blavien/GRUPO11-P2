import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClient {
    private static final Scanner in = new Scanner(System.in);
    private static ArrayList<Client> clients = new ArrayList<>();
    public static void main ( String[] args ) throws Exception {
        boolean mainAlive = true;
        while (mainAlive){
            boolean subAlive = true;

            System.out.println("\n*************** Welcome to GROUP 11 ******************************\n");
            System.out.println("1. Start totally new session");
            System.out.println("2. Go back to previous session");
            System.out.println("2. Quit");
            int input = in.nextInt();
            switch(input){
                case 1:
                    Client client = new Client ( 8000 );

                    String name = client.getClientName();

                    clients.add(client);

                    Client actualClient = null;

                    for (Client client1 : clients){
                        if(client1.getClientName().equals(name)){
                            actualClient = client1;
                        }
                    }

                    RequestUtils.writeNumberToFile(0,RequestUtils.HANDSHAKE_SIGNAL); // 0 - Nothing

                    RequestUtils.newClientRegister(actualClient.getClientName()); //Register new clients

                    if(RequestUtils.requestLimit(actualClient.getClientName()) == 0){   //Request counter do client == 0  - novo ou requests levou reset

                        RequestUtils.writeNumberToFile(1,RequestUtils.HANDSHAKE_SIGNAL); // 1 - Handshake

                        actualClient.doHandshake();

                    }
                    RequestUtils.writeNumberToFile(0,RequestUtils.HANDSHAKE_SIGNAL); // 1 - Handshake
                    while (subAlive == true){

                        if(RequestUtils.requestLimit(actualClient.getClientName()) == 5){

                            System.out.println("\nYou have reached max requests. You will be taken to the session restart.");

                            actualClient.endConnection();

                            break;
                        }

                        System.out.println("\n*************** USER : "+actualClient.getClientName()+" *******************");
                        System.out.println("\n"+RequestUtils.requestLimit(actualClient.getClientName())+"\n");
                        System.out.println("\n1. Request file");
                        System.out.println("2. End session");
                        int req = in.nextInt();

                        switch (req){
                            case 1:
                                actualClient.execute();
                                break;
                            case 2:
                                subAlive = false;
                                break;
                        }
                    }
                    break;

                case 2:
                    System.out.println("\nYou already had an acoount, you want to go back to that one.");
                    System.out.println("\nSo ... What's you name ? ");
                    String c_name = in.next();
                    boolean clientFound = false;
                    Client ourClient = null;

                    for (Client client1 : clients){
                        if(client1.getClientName().equals(c_name)){
                            ourClient = client1;
                            clientFound = true;
                        }
                    }

                    if(clientFound == false){
                        System.out.println("\nSorry broski, you don't have an active client account. Go do it again.");
                        break;
                    }else{


                        while (subAlive == true){

                            if(RequestUtils.requestLimit(ourClient.getClientName()) == 5){

                                System.out.println("\nYou have reached max requests. You will be taken to the session restart.");

                                ourClient.endConnection();

                                clients.remove(ourClient);
                                break;
                            }

                            System.out.println("\n*************** USER : "+ourClient.getClientName()+" *******************");
                            System.out.println("\n"+RequestUtils.requestLimit(ourClient.getClientName())+"\n");
                            System.out.println("\n1. Request file");
                            System.out.println("2. End session");
                            int req = in.nextInt();

                            switch (req){
                                case 1:
                                    ourClient.execute();
                                    break;
                                case 2:
                                    subAlive = false;
                                    break;
                            }
                        }
                    }

                    break;
                case 3:
                    mainAlive = false;
                    break;
            }
        }

    }

}
