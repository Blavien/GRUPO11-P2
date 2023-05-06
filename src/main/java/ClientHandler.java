import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the client handler. It handles the communication with the client. It reads the file from the
 * server and sends it to the client.
 */
public class ClientHandler extends Thread {
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket client;
    private final boolean isConnected;
    private List<String> requestSplit;
    private byte[] message;

    // Initialize HashMap to keep track of request counts for each client


    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler (Socket client, byte[] message) throws IOException {
        this.client = client;
        in = new ObjectInputStream ( this.client.getInputStream ( ) );
        out = new ObjectOutputStream ( this.client.getOutputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
    }

    @Override
    public void run ( ) {
        super.run ( );
        try {
            while ( isConnected ) {

                String request = message.toString();

                System.out.println(request);

                System.out.println("\n***** SERVER *****\n"+ request);
                //Splits message received
                requestSplit = RequestUtils.splitRequest(request);
                //Regista os número de pedidos feitos por este utilizador
                RequestUtils.registerRequests ((ArrayList<String>) requestSplit);

                // Reads the file and sends it to the client
                byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( requestSplit.get(1) ) );
                sendFile ( content );
            }
            // Close connection
            closeConnection ( );
        } catch ( IOException | ClassNotFoundException e ) {
            // Close connection
            closeConnection ( );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Sends the file to the client
     *
     * @param content the content of the file to send
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content) throws Exception {
        Message response = new Message ( content );
        out.writeObject ( response);
        out.flush ( );
    }


    /**
     * Closes the connection by closing the socket and the streams.
     */
    private void closeConnection ( ) {
        try {
            client.close ( );
            out.close ( );
            in.close ( );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

}