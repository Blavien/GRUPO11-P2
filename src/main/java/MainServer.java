import java.io.IOException;
import java.security.KeyPair;

public class MainServer {

    public static void main ( String[] args ) throws Exception {
        Server server = new Server ( 8000 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );
    }

}
