import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a server that receives a message from the clients. The server is implemented as a thread. Each
 * time a client connects to the server, a new thread is created to handle the communication with the client.
 */
public class Server implements Runnable {
    public static final String FILE_PATH = "server/files";
    private final ServerSocket server;
    private final boolean isConnected;
    private final PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;
    private static HashMap<PublicKey, Integer> requestCounter;

    /**
     * Constructs a Server object by specifying the port number. The server will be then created on the specified port.
     * The server will be accepting connections from all local addresses.
     *
     * @param port the port number
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public Server ( int port ) throws Exception {
        this.requestCounter = new HashMap<>();
        server = new ServerSocket ( port );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        KeyPair keyPair = RSA.generateKeyPair();
        this.privateRSAKey = keyPair.getPrivate();
        this.publicRSAKey = keyPair.getPublic();
    }
    public static void updateRequestCounter(PublicKey clientPublicKey){
        if (requestCounter.containsKey(clientPublicKey)) {
            // Increment request count for client
            int currentCount = requestCounter.get(clientPublicKey);
            requestCounter.put(clientPublicKey, currentCount + 1);
        } else {
            // Add client to HashMap with request count of 1
            requestCounter.put(clientPublicKey, 1);
        }
    }
    public void printMap(){
        // Print the map
        for (Map.Entry<PublicKey, Integer> entry : requestCounter.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
    @Override
    public void run ( ) {
        try {
            while ( isConnected ) {
                Socket client = server.accept ( );
                // Process the request
                printMap();
                process ( client );
            }
            closeConnection ( );
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    /**
     * Processes the request from the client.
     *
     * @throws IOException if an I/O error occurs when reading stream header
     */
    private void process ( Socket client ) throws IOException {
        ClientHandler clientHandler = new ClientHandler ( client );
        clientHandler.start ( );
    }

    /**
     * Closes the connection and the associated streams.
     */
    private void closeConnection ( ) {
        try {
            server.close ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

}