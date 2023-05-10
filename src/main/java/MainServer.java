import java.io.IOException;
import java.security.KeyPair;

public class MainServer {
    /**
     * This main is executed to allow a serverThread to be created, allowing interaction with the client
     *
     * @throws Exception
     */
    public static void main ( String[] args ) throws Exception {
        RequestUtils.emptyRegistry();

        Server server = new Server ( 8000 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );
    }

}
