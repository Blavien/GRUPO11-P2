import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a server that receives a message from the clients. The server is implemented as a thread. Each
 * time a client connects to the server, a new thread is created to handle the communication with the client.
 */
public class Server implements Runnable {
    public static final String FILE_PATH = "server/files";
    public static final String server_name = "Server_G11";
    private final ServerSocket server;
    private final boolean isConnected;
    private final PrivateKey privateRSAKey;
    private final PublicKey publicRSAKey;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String message;
    private ArrayList<String> requestSplit;
    public static boolean doHandshake = false;
    private PublicKey clientPublicRSAKey;

    private static boolean dividedMessage;

    private static int dividedFinished;
    /**
     * Constructs a Server object by specifying the port number. The server will be then created on the specified port.
     * The server will be accepting connections from all local addresses.
     *
     * @param port the port number
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public Server ( int port ) throws Exception {
        server = new ServerSocket ( port );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        KeyPair keyPair = RSA.generateKeyPair();
        this.privateRSAKey = keyPair.getPrivate();
        this.publicRSAKey = keyPair.getPublic();
        dividedMessage = false;
        dividedFinished = 0;
        RSA.storeRSAKeys(keyPair,server_name);
    }
    public void setMessage(byte[] message){
        this.message = (new String (message));
    }
    public String getMessage(){
        return this.message;
    }
    public PublicKey doHandshake(Socket client) throws Exception {
        in = new ObjectInputStream( client.getInputStream ( ) );
        out = new ObjectOutputStream( client.getOutputStream ( ) );
        clientPublicRSAKey = rsaKeyDistribution ( in );

        //System.out.println("Handshake was done");
        return clientPublicRSAKey;
    }
    @Override
    public void run ( ) {
        try {
            // Perform key distribution
            while ( isConnected ) {
                Socket client = server.accept ( );

                System.out.println("Client connected");
                doHandshake(client);
                int i = 0;
                while (i != 5){
                    clientHandlerProcess(process ( in , clientPublicRSAKey ));
                    i++;
                }

            }
            closeConnection ( );
        } catch ( Exception e ) {
            throw new RuntimeException ( e );
        }
    }

    private byte[] process ( ObjectInputStream in , PublicKey senderPublicRSAKey ) throws Exception {
        // Agree on a shared secret
        BigInteger sharedSecret = agreeOnSharedSecret ( senderPublicRSAKey );
        // Reads the message object
        Message messageObj = ( Message ) in.readObject ( );
        // Extracts and decrypt the message
        byte[] decryptedMessage = AES.decrypt ( messageObj.getMessage ( ) , sharedSecret.toByteArray ( ) );
        // Computes the digest of the received message
        byte[] computedDigest = Integrity.generateDigest ( decryptedMessage );
        // Verifies the integrity of the message
        if ( ! Integrity.verifyDigest ( messageObj.getSignature ( ) , computedDigest ) ) {
            throw new RuntimeException ( "The integrity of the message is not verified" );
        }
        System.out.println("Aqui está o conteudo do request desencriptado ");
        System.out.println(new String(decryptedMessage));
        return decryptedMessage;
    }
    /**
     * Performs the Diffie-Hellman algorithm to agree on a shared private key.
     *
     * @param senderPublicRSAKey the public key of the sender
     *
     * @return the shared secret key
     *
     * @throws Exception when the key agreement protocol fails
     */
    private BigInteger agreeOnSharedSecret ( PublicKey senderPublicRSAKey ) throws Exception {
        // Generate a pair of keys
        BigInteger privateKey = DiffieHellman.generatePrivateKey ( );
        BigInteger publicKey = DiffieHellman.generatePublicKey ( privateKey );
        // Extracts the public key from the request
        BigInteger clientPublicKey = new BigInteger ( RSA.decryptRSA ( ( byte[] ) in.readObject ( ) , senderPublicRSAKey ) );
        // Send the public key to the client
        sendPublicDHKey ( publicKey );
        // Generates the shared secret
        return DiffieHellman.computePrivateKey ( clientPublicKey , privateKey );
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

    private PublicKey rsaKeyDistribution ( ObjectInputStream in ) throws Exception {
        // Extract the public key
        System.out.println("Reading client pubk");
        PublicKey senderPublicRSAKey = ( PublicKey ) in.readObject ( );

        System.out.println("Read client pubk");
        // Send the public key
        sendPublicRSAKey ( );
        System.out.println("Sending mine");
        return senderPublicRSAKey;
    }
    private void sendPublicRSAKey ( ) throws IOException {
        System.out.println("SERVER SENDING THEM PUK : ");
        out.writeObject ( publicRSAKey );
        out.flush ( );
    }

    private void sendPublicDHKey ( BigInteger publicKey ) throws Exception {
        out.writeObject ( RSA.encryptRSA ( publicKey.toByteArray ( ) , this.privateRSAKey ) );
    }

    public void processCH (Socket client, byte[] message){
        try {
            ClientHandler clienth = new ClientHandler(client,message);
            clienth.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    //SE O CH FUNCIONA-SE ISTO ESTARIA NO CLIENT HANDLER
    public void clientHandlerProcess(byte[] message) throws Exception {
        String request = new String(message);
        System.out.println("\n***** SERVER *****\n"+ request);
        //Splits message received
        requestSplit = RequestUtils.splitRequest(request);
        //Regista os número de pedidos feitos por este utilizador
        RequestUtils.registerRequests (requestSplit);
        // Reads the file and sends it to the client
        byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( requestSplit.get(1) ) );
        String auxContent = new String(content);
        System.out.println("Aqui está o conteudo do ficheiro que será enviado: ");
        System.out.println(auxContent);


        System.out.println("Aqui está o conteudo dividido do ficheiro que será enviado: ");
        int contentSize = content.length;
        int numParts = (int) Math.ceil((double) contentSize / 2048);
        setDividedFinished(numParts);
        if(contentSize>2048) {
            setDividedMessage(true);
            List<String> contentDividido = splitStringBySize(auxContent);
            for (int i = 0; i < contentDividido.size(); i++) {
                System.out.println(contentDividido.get(i));
                sendFile(contentDividido.get(i).getBytes());
                System.out.println("Fim de uma mensagem");
            }
            setDividedMessage(false);
        }
        else {

            sendFile(content);

        }

    }

    public static List<String> splitStringBySize(String input) {
        List<String> output = new ArrayList<>();

        // Verifica o tamanho da string em bytes
        int inputSize = input.getBytes().length;

        if (inputSize <= 2048) { // Se o tamanho for menor ou igual a 2KB, adiciona a string inteira na lista de saída
            output.add(input);
        } else { // Caso contrário, divide a string em pedaços de no máximo 2KB
            int numParts = (int) Math.ceil((double) inputSize / 2048); // Calcula o número de pedaços necessários
            int remainingBytes = inputSize;
            int start = 0;
            int end = 0;

            for (int i = 0; i < numParts; i++) {
                end += (remainingBytes > 2048) ? 2048 : remainingBytes; // Define o final do pedaço, garantindo que não ultrapasse 2KB
                String part = input.substring(start, end); // Extrai o pedaço da string original
                output.add(part); // Adiciona o pedaço na lista de saída
                start = end;
                remainingBytes = inputSize - end;
            }
        }

        return output;
    }


    private void sendFile ( byte[] content ) throws Exception {


        // Agree on a shared secret
        BigInteger sharedSecret = agreeOnSharedSecret ( clientPublicRSAKey );
        // Encrypts the message
        byte[] encryptedResponse = AES.encrypt ( content, sharedSecret.toByteArray ( ) );
        // Generates the MAC
        byte[] digest = Integrity.generateDigest ( content);
        // Creates the message object
        Message responseObj = new Message ( encryptedResponse , digest );
        // Sends the encrypted message
        out.writeObject ( responseObj );
        out.flush ( );
    }

    private void setDividedMessage(boolean state){
        dividedMessage=state;
    }
    public static boolean getDividedMessage(){
        return dividedMessage;
    }

    public static void setDividedFinished(int times){
        dividedFinished=times;
    }
    public static int getDividedFinished(){
        return dividedFinished;
    }
}