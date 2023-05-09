import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
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
    private ArrayList<String> requestSplit;
    private SecretKey clientMACKey;
    private static String DIGEST_ALGORITHM ="";
    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler (Socket client) throws Exception {
        this.client = client;
        in = new ObjectInputStream ( this.client.getInputStream ( ) );
        out = new ObjectOutputStream ( this.client.getOutputStream ( ) );
    }

    @Override
    public void run ( ) {
        super.run ( );
        try {
            sleep(2000);
            PublicKey clientPublicRSAKey;

            while(RequestUtils.readNumberFromFile(RequestUtils.HANDSHAKE_SIGNAL) != 1){  }

            clientPublicRSAKey = rsaKeyDistribution ( in );
            BigInteger sharedSecret = agreeOnSharedSecret ( clientPublicRSAKey );
            System.out.println("SERVER : Handshake was a sucess.");


            ArrayList<Integer> clientChoice = receiveClientChoice();
            System.out.print("SERVER : Client setup ");
            setupClientChoice(clientChoice);

            this.clientMACKey = receiveMacKey();
            System.out.println("SERVER : MAC Key received.");

            int i = 0;
            while ( i != 5) {

                byte[] message = process ( in , sharedSecret.toByteArray ( ), clientChoice);

                if(message != null ){
                    System.out.println("\n***** REQUEST *****\n"+ new String(message));

                    //Splits message received

                    requestSplit = RequestUtils.splitRequest(new String(message));

                    //Regista os número de pedidos feitos por este utilizador

                    RequestUtils.registerRequests (requestSplit);
                    // Reads the file and sends it to the client

                    byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( requestSplit.get(1) ) );

                    String auxContent = new String(content);
                    System.out.println("\n***** FILE ********\nHere's the file content: ");
                    System.out.println(auxContent);


                    if(content.length>2048) {

                        sendFile("INICIO".getBytes(), sharedSecret.toByteArray (), clientChoice);

                        ArrayList<byte[]> contentDividido = ByteUtils.splitByteArray(auxContent.getBytes(),2048 );

                        for (byte[] bytes : contentDividido) {

                            sendFile(bytes, sharedSecret.toByteArray(), clientChoice);
                            System.out.println(new String(bytes));

                        }
                        sendFile("FIM".getBytes(), sharedSecret.toByteArray (), clientChoice);
                    }
                    else {

                        sendFile(content, sharedSecret.toByteArray (), clientChoice);

                    }
                    i = RequestUtils.getRequestCounter(requestSplit.get(0));
                }
            }

            System.out.println("\nClient "+requestSplit.get(0) +" exceeded the max requests.");
            RequestUtils.resetRequestCounter(requestSplit.get(0));
            System.out.println("Client "+requestSplit.get(0) +" request counter has been reset to 0.");

            System.out.println("Closing socket connection.");

            closeConnection ( );
        } catch ( IOException | ClassNotFoundException e ) {

            closeConnection ( );

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
    public void setupClientChoice(ArrayList<Integer> clientChoice){
        switch (clientChoice.get(0)) {
            case 0 -> {
                DIGEST_ALGORITHM = "HmacSHA512";
                System.out.print("[ " + DIGEST_ALGORITHM + ", ");
            }
            case 1 -> {
                DIGEST_ALGORITHM = "HmacSHA256";
                System.out.print("[ " + DIGEST_ALGORITHM + ", ");
            }
            case 2 -> {
                DIGEST_ALGORITHM = "HmacSHA1";
                System.out.print("[ " + DIGEST_ALGORITHM + ", ");
            }
            case 3 -> {
                DIGEST_ALGORITHM = "HmacMD5";
                System.out.print("[ " + DIGEST_ALGORITHM + ", ");
            }
        }
        switch (clientChoice.get(1)) {
            case 0 -> System.out.print("AES ]");
            case 1 -> System.out.print("DES ]");
            case 2 -> System.out.print("3DES ]");
        }
        System.out.println("\n");
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
                end += Math.min(remainingBytes, 2048); // Define o final do pedaço, garantindo que não ultrapasse 2KB
                String part = input.substring(start, end); // Extrai o pedaço da string original
                output.add(part); // Adiciona o pedaço na lista de saída
                start = end;
                remainingBytes = inputSize - end;
            }
        }

        return output;
    }

    /**
     * Sends the file to the client
     *
     * @param content the content of the file to send
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content , byte[] sharedSecret , ArrayList<Integer> choice) throws Exception {
        byte[] encryptedResponse = null;

        if(choice.get(1) == 0){ //AES
            encryptedResponse = AES.encrypt ( content , sharedSecret );
        }
        if(choice.get(1) == 1){ //DES
            encryptedResponse = DES.encrypt ( content , sharedSecret );
        }
        if(choice.get(1) == 2){ //DES
            encryptedResponse = DES3.encrypt ( content , sharedSecret );
        }

        byte[] digest = Hmac.hmac(content, DIGEST_ALGORITHM,clientMACKey.getEncoded());
        // Creates the message object
        Message responseObj = new Message ( encryptedResponse , digest );
        // Sends the encrypted message
        out.writeObject ( responseObj );
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
    private SecretKey receiveMacKey() throws IOException, ClassNotFoundException {
        return  ( SecretKey ) in.readObject ( );
    }
    private ArrayList<Integer> receiveClientChoice () throws IOException, ClassNotFoundException {
        return (ArrayList<Integer>) in.readObject();
    }
    //DIFFIE HELLLMAN
    private PublicKey rsaKeyDistribution (ObjectInputStream in ) throws Exception {
        // Extract the public key
        PublicKey senderPublicRSAKey = ( PublicKey ) in.readObject ( );
        // Send the public key
        sendPublicRSAKey ( );
        return senderPublicRSAKey;
    }
    private void sendPublicRSAKey ( ) throws Exception {
        out.writeObject ( Server.getPublicRSAKey());
        out.flush ( );
    }

    private void sendPublicDHKey ( BigInteger publicKey ) throws Exception {
        out.writeObject ( RSA.encryptRSA ( publicKey.toByteArray ( ) , Server.getPrivateRSAKey()));
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

    private byte[] process ( ObjectInputStream in , byte[] sharedSecret, ArrayList<Integer> clientChoice ) throws Exception {
        // Reads the message object
        Message messageObj = ( Message ) in.readObject ( );
        // Extracts and decrypt the message

        byte[] decryptedMessage = null;
        if(clientChoice.get(1) == 0){
            decryptedMessage = AES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
        }
        if(clientChoice.get(1) == 1){
            decryptedMessage = DES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
        }
        if(clientChoice.get(1) == 2){
            decryptedMessage = DES3.decrypt ( messageObj.getMessage ( ) , sharedSecret );
        }

        // Computes the digest of the received message
        byte[] computedDigest = Hmac.hmac(decryptedMessage, DIGEST_ALGORITHM,clientMACKey.getEncoded());
        if ( ! Hmac.verifyDigest ( messageObj.getSignature ( ) , computedDigest ) ) {
            throw new RuntimeException ( "The integrity of the message is not verified" );
        }

        return decryptedMessage;
    }
}
