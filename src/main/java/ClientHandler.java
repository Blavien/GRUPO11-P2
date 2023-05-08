import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
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
    private final boolean isConnected;
    private ArrayList<String> requestSplit;
    private byte[] message;
    private static boolean dividedMessage;
    private static int dividedFinished;

    /**
     * Creates a ClientHandler object by specifying the socket to communicate with the client. All the processing is
     * done in a separate thread.
     *
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public ClientHandler (Socket client) throws Exception {
        this.handshakeReg = new HashMap<>();
        this.client = client;
        in = new ObjectInputStream ( this.client.getInputStream ( ) );
        out = new ObjectOutputStream ( this.client.getOutputStream ( ) );
        isConnected = true; // TODO: Check if this is necessary or if it should be controlled
        dividedMessage = false;
        dividedFinished = 0;
    }

    @Override
    public void run ( ) {
        super.run ( );
        try {
            sleep(2000);
            PublicKey clientPublicRSAKey = null;

            while(RequestUtils.readNumberFromFile(RequestUtils.HANDSHAKE_SIGNAL) != 1){ }

            clientPublicRSAKey = rsaKeyDistribution ( in );

            int i = 0;

            System.out.println("SERVER : Handshake was a sucess.");

            while ( i != 5) {

                byte[] message = process (in , clientPublicRSAKey );

                if(message != null ){
                    System.out.println("\n***** REQUEST *****\n"+ new String(message));

                    //Splits message received

                    requestSplit = RequestUtils.splitRequest(new String(message));

                    //Regista os número de pedidos feitos por este utilizador

                    RequestUtils.registerRequests (requestSplit);

                    // Reads the file and sends it to the client

                    byte[] content = FileHandler.readFile ( RequestUtils.getAbsoluteFilePath ( requestSplit.get(1) ) );

                    String auxContent = new String(content);
                    System.out.println("Aqui está o conteudo do ficheiro que será enviado: ");
                    System.out.println(auxContent);



                    int contentSize = content.length;
                    int numParts = (int) Math.ceil((double) contentSize / 2048);
                    //setDividedFinished(numParts);

                    if(contentSize>2048) {
                        //setDividedMessage(true);
                        sendFile("INICIO".getBytes(), clientPublicRSAKey);
                        ArrayList<byte[]> contentDividido = ByteUtils.splitByteArray(auxContent.getBytes(),numParts );

                        for (int j = 0; j < contentDividido.size(); j++) {

                            sendFile(contentDividido.get(j), clientPublicRSAKey);
                            String ola = new String(contentDividido.get(j));
                            System.out.println(ola);
                            System.out.println("Fim de uma mensagem.");

                        }

                        sendFile("FIM".getBytes(), clientPublicRSAKey);
                    }
                    else {

                        sendFile(content, clientPublicRSAKey);

                    }
                    i = RequestUtils.requestLimit(requestSplit.get(0));

                    System.out.println(requestSplit.get(0)+" - Request counter: "+i);

                }
            }
            if(i >= 5){

                System.out.println("\n Client exceeded the max requests.");

                RequestUtils.resetRequestCounter(requestSplit.get(0));

                System.out.println("Client's request counter has been reset to 0.");
            }

            System.out.println("Closing socket connection.");

            closeConnection ( );
        } catch ( IOException | ClassNotFoundException e ) {

            closeConnection ( );

        } catch (Exception e) {

            throw new RuntimeException(e);

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



    /**
     * Sends the file to the client
     *
     * @param content the content of the file to send
     *
     * @throws IOException when an I/O error occurs when sending the file
     */
    private void sendFile ( byte[] content , PublicKey clientPublicRSAKey) throws Exception {


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
        return decryptedMessage;
    }

}
