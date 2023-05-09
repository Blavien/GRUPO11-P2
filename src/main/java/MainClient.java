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
            boolean handshakeInsuccess = false;
            System.out.println("\n*************** Welcome to GROUP 11 ******************************\n");
            System.out.println("1. Start a new session");
            System.out.println("2. Go back to a previous session");
            System.out.println("3. Quit");
            int input = in.nextInt();
            switch(input){
                case 1:
                    boolean clientAlreadyExists = false;
                    Client client = new Client ( 8000 );
                    String name = client.getClientName();

                    //Checks f client is already registered on the Registry.txt
                    // Supostamente, se o cliente esta dentro do array clients - ele já está registado
                    for (Client client1 : clients){
                        if(client1.getClientName().equals(name)){
                            clientAlreadyExists = true;
                        }
                    }
                    if(clientAlreadyExists){
                        System.out.println("You already have an account with that username. If you want a new account click 1, else click 2.");
                    }else{

                        clients.add(client);

                        RequestUtils.writeNumberToFile(0,RequestUtils.HANDSHAKE_SIGNAL); // 0 - Nothing

                        if(RequestUtils.requestLimit(client.getClientName()) == 0){   //Request counter do client == 0  - novo ou requests levou reset

                            RequestUtils.writeNumberToFile(1,RequestUtils.HANDSHAKE_SIGNAL); // 1 - Handshake

                            handshakeInsuccess = client.doHandshake();
                        }

                        if(handshakeInsuccess == true){
                            System.out.println("\nYou have choosen POORLY, so go do a new session.");
                            clients.remove(client);
                            break;
                        }

                        RequestUtils.newClientRegister(client.getClientName()); //Fica registado se o hadnshake foi um sucesso

                        RequestUtils.writeNumberToFile(0,RequestUtils.HANDSHAKE_SIGNAL); // 1 - Handshake
                        while (subAlive == true){

                            if(RequestUtils.requestLimit(client.getClientName()) == 5){

                                System.out.println("\nYou have reached max requests. You will be taken to the session restart.");
                                clients.remove(client);
                                client.endConnection();
                                break;
                            }

                            System.out.println("\n*************** USER : "+client.getClientName()+" *******************");
                            System.out.println("Requests: "+RequestUtils.requestLimit(client.getClientName())+"\n");
                            System.out.println("1. Request file");
                            System.out.println("2. Go to main menu");
                            int req = in.nextInt();

                            switch (req){
                                case 1:
                                    client.execute("");
                                    break;
                                case 2:
                                    subAlive = false;
                                    break;
                            }
                        }
                        break;
                    }
                case 2:
                    System.out.println("\nYou already had an account, you want to go back to that one.");
                    System.out.println("\nWhat's you name ? ");
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
                        System.out.println("\nSorry bro, we don't have an active session for that client. Go do it again.");
                        break;
                    }else{
                        while (subAlive == true){

                            if(RequestUtils.requestLimit(ourClient.getClientName()) == 5){

                                System.out.println("\nYou have reached max requests. You will be taken to the session restart.");
                                clients.remove(ourClient);
                                ourClient.endConnection();
                                break;
                            }

                            System.out.println("\n*************** USER : "+ourClient.getClientName()+" *******************");
                            System.out.println("\n"+RequestUtils.requestLimit(ourClient.getClientName())+"\n");
                            System.out.println("\n1. Request file");
                            System.out.println("2. Go to main menu");
                            int req = in.nextInt();

                            switch (req){
                                case 1:
                                    ourClient.execute("");
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
