import java.security.KeyPair;
import java.util.Scanner;

public class MainClient {
    private static final Scanner in = new Scanner(System.in);
    public static void main ( String[] args ) throws Exception {

        System.out.println("Welcome to GROUP 11 2. project!!\n");
        System.out.println("1. Start your session");
        int input = in.nextInt();
        switch(input){
            case 1:
                Client client = new Client ( 8000 );

                client.setClientName("Sara");
                RSA.generateKeyPair();
                RSA.storeRSAKeys ( RSA.generateKeyPair(),client.getClientName());
                client.setPrivateKey();
                client.setPublicKey();

                client.execute();
        }
    }

}
