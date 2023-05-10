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

    /**
     *
     * This creates a thread for every client that connects to the server
     * The client signals the server/clientHandler that it wants to do the Handshake
     * And while the reading isn't done, because it might fail and read null, it stays stuck on that while loop
     *
     * It does the handshake() and agrees on the shared secret that can be used for the encryption/decryption
     * Receives the clientChoice(), the array that contains the Hashing & Encryption algorithms
     * Receives the unique Mac Key that's going to be used for the hashing, so it guarantees the authenticity and integrity of the message. This key is redone every 5 requests.
     *
     * Now we have a while loop that's to control the 5 requests, sure we could've made this is a way mroe effecient way but we got no time for that. It registers on the Registry.txt every request done by every Client
     * RequestSplit() gives us info about the request done by the client, and the client username
     *
     * Then we have another requisite, if the message is too big we split it into smaller chunks and pad it, to be sent to the client.
     *
     * After the 5 requests are done, this thread dies, and the client disconnects.
     *
     */
    @Override
    public void run ( ) {
        super.run ( );
        try {

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

    /**
     *
     * @param clientChoice Receives an arraylist named clientChoice that contains the encryption method that was choosen
     *                     by the user.
     */
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



    /**
     * Sends the file to the client
     *
     * @param content the content of the file to send
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content , byte[] sharedSecret , ArrayList<Integer> choice) throws Exception {
        byte[] encryptedResponse = null;

        switch (choice.get(1)){
            case 0:
                encryptedResponse = AES.encrypt ( content , sharedSecret );
                break;
            case 1:
                encryptedResponse = DES.encrypt ( content , sharedSecret );
                break;
            case 2:
                encryptedResponse = DES3.encrypt ( content , sharedSecret );
                break;
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

    /**
     * MacKey is going to be sent by the side of the client and it will be returned read and returned in this function
     * @return MacKey that was readed
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private SecretKey receiveMacKey() throws IOException, ClassNotFoundException {
        return  ( SecretKey ) in.readObject ( );
    }

    /**
     *  The Arraylist containing the choices of the decryption methods chosen by the user will be written as an object by the
     *  user, this method simply reads it and returns it.
     * @return Arraylist containing the choices of the user decryption method
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList<Integer> receiveClientChoice () throws IOException, ClassNotFoundException {
        return (ArrayList<Integer>) in.readObject();
    }

    /**
     * This method is used to receive the public key of the user
     * @param in InputStream that ables the server to read the key sent by the user as an object
     * @return Returns the public key that was read
     * @throws Exception
     */
    //DIFFIE HELLLMAN
    private PublicKey rsaKeyDistribution (ObjectInputStream in ) throws Exception {
        // Extract the public key
        PublicKey senderPublicRSAKey = ( PublicKey ) in.readObject ( );
        // Send the public key
        sendPublicRSAKey ( );
        return senderPublicRSAKey;
    }

    /**
     *Sends the server's public key as an object
     * @throws Exception
     */
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

    /**
     *
     * @param in
     * @param sharedSecret
     * @param clientChoice
     * @return
     * @throws Exception
     */

    private byte[] process ( ObjectInputStream in , byte[] sharedSecret, ArrayList<Integer> clientChoice ) throws Exception {
        // Reads the message object
        Message messageObj = ( Message ) in.readObject ( );
        // Extracts and decrypt the message

        byte[] decryptedMessage = null;

        switch (clientChoice.get(1)){
            case 0:
                decryptedMessage = AES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
                break;
            case 1:
                decryptedMessage = DES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
                break;
            case 2:
                decryptedMessage = DES3.decrypt ( messageObj.getMessage ( ) , sharedSecret );
                break;
        }

        // Computes the digest of the received message
        byte[] computedDigest = Hmac.hmac(decryptedMessage, DIGEST_ALGORITHM,clientMACKey.getEncoded());
        if ( ! Hmac.verifyDigest ( messageObj.getSignature ( ) , computedDigest ) ) {
            throw new RuntimeException ( "The integrity of the message is not verified" );
        }

        return decryptedMessage;
    }
}
