import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the client handler. It handles the communication with the client. It reads the file from the
 * server and sends it to the client.
 */
public class ClientHandler extends Thread {
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket client;
    private final String REGISTRY_PATH = "server/Registry/registry.txt";
    private final boolean isConnected;
    private ArrayList<String> requestSplit;
    private final int MAX_REQUESTS = 5;
    // Initialize HashMap to keep track of request counts for each client


    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @param client the socket to communicate with the client
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler ( Socket client ) throws IOException {
        this.client = client;
        in = new ObjectInputStream ( client.getInputStream ( ) );
        out = new ObjectOutputStream ( client.getOutputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
    }
    public ArrayList<String> splitRequest (String message) throws Exception{
        ArrayList<String> requestInfo = new ArrayList<>();

        String[] splitMessage =message.split(": ");
        //Splits to get the client name
        requestInfo.add(splitMessage[1]);
        //Splits to get the request file
        requestInfo.add(splitMessage[3]);
        return requestInfo;
    }
    public void registerRequests (ArrayList<String> request) throws IOException {
        String client_name = request.get(0);
        File file = new File(REGISTRY_PATH);
        int request_counter = 1; //For the first request
        boolean userFound = false;

        // Se exisitir ele vai lá procurar o client name e incrementar o counter
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(client_name)) {
                    request_counter = Integer.parseInt(parts[1]) + 1;
                    userFound = true;
                    break;
                }
            }
            br.close();
        }

        String data = client_name + " : " + request_counter;

        if (userFound) {
            List<String> lines = Files.readAllLines(file.toPath());
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                if (line.startsWith(client_name + " : ")) {
                    out.println(data);
                } else {
                    out.println(line);
                }
            }
            out.close();
        } else {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(data);
            out.close();
        }
    }
    public boolean requestLimit (String request) throws IOException{
        String client_name = request;

        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        int requestCounter = 0;

        // Search for client name and update request counter
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(client_name)) {
                    requestCounter = Integer.parseInt(parts[1]);
                    userFound = true;
                    break;
                }
            }
            br.close();
        }

        // Check if request limit reached
        if (userFound && requestCounter == 5) {
            return true;
        } else {
            return false;
        }
    }
    public void resetRequestCounter(String username) throws IOException {
        File file = new File(REGISTRY_PATH);
        boolean userFound = false;
        List<String> lines = new ArrayList<>();

        // Search for the user in the file and replace the request counter with 1
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts[0].equals(username)) {
                    lines.add(parts[0] + " : 1");
                    userFound = true;
                } else {
                    lines.add(line);
                }
            }
            br.close();
        }

        // Write the modified file
        if (userFound) {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                out.println(line);
            }
            out.close();
        }
    }
    @Override
    public void run ( ) {
        super.run ( );
        try {
            while ( isConnected ) {
                // Reads the message to extract the path of the file
                Message message = ( Message ) in.readObject ( );
                String request = new String ( message.getMessage ( ) );

                //Splits message received
                requestSplit = splitRequest(request);
                //Regista os número de pedidos feitos por este utilizador
                if(requestLimit (requestSplit.get(0))) {
                    Message warningMessage = new Message("Request limit reached for this user.".getBytes());
                    out.writeObject(warningMessage);
                    out.flush();
                    closeConnection ( );
                }else{
                    //This is here so if this request is 5, it can still pass, but next request it's gonna get rejected
                    registerRequests (requestSplit);
                    // Reads the file and sends it to the client
                    byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( requestSplit.get(1) ) );
                    sendFile ( content );
                }

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
    private void sendFile ( byte[] content ) throws IOException {
        Message response = new Message ( content );
        out.writeObject ( response );
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
