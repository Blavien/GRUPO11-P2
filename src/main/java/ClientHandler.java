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
    private ArrayList<Integer> clientChoice;

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
        boolean isConnected = true; // TODO: Check if this is necessary or if it should be controlled
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


            clientChoice = receiveClientChoice();
            System.out.println("SERVER : Received the client's alorithms choice.");
            printClientChoice(clientChoice);

            if(clientChoice.get(0) == 0){
                clientMACKey = receiveMacKey();
                System.out.println("SERVER : MACK Key received.");
            }

            int i = 0;
            while ( i != 5) {

                byte[] message = process ( in , sharedSecret.toByteArray ( ), clientChoice );

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

                    int contentSize = content.length;
                    //int numParts = (int) Math.ceil((double) contentSize / 2048);

                    if(contentSize>2048) {

                        sendFile("INICIO".getBytes(), sharedSecret.toByteArray (), clientChoice);

                        ArrayList<byte[]> contentDividido = ByteUtils.splitByteArray(auxContent.getBytes(),2048 );

                        for (int j = 0; j < contentDividido.size(); j++) {

                            sendFile(contentDividido.get(j), sharedSecret.toByteArray (), clientChoice);
                            System.out.println(new String(contentDividido.get(j)));

                        }
                        sendFile("FIM".getBytes(), sharedSecret.toByteArray (), clientChoice);
                    }
                    else {

                        sendFile(content, sharedSecret.toByteArray (), clientChoice);

                    }
                    i = RequestUtils.requestLimit(requestSplit.get(0));

                    //System.out.println(requestSplit.get(0)+" - Request counter: "+i);

                }
            }
            if(i >= 5){

                System.out.println("\nClient "+requestSplit.get(0) +" exceeded the max requests.");

                RequestUtils.resetRequestCounter(requestSplit.get(0));

                System.out.println("Client "+requestSplit.get(0) +" request counter has been reset to 0.");
            }

            System.out.println("Closing socket connection.");

            closeConnection ( );
        } catch ( IOException | ClassNotFoundException e ) {

            closeConnection ( );

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
    public void printClientChoice(ArrayList<Integer> clientChoice){
        if(clientChoice.get(0) == 0){
            System.out.print("[ MAC , ");
            if(clientChoice.get(1) == 0){
                System.out.print(" AES ]");
            } else if (clientChoice.get(1) == 1) {
                System.out.print(" 3DES ]");
            }
        }else if (clientChoice.get(0) == 1){
            System.out.print("[ HASH , ");
            if(clientChoice.get(1) == 0){
                System.out.print(" AES ]");
            } else if (clientChoice.get(1) == 1) {
                System.out.print(" 3DES ]");
            }
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
    private void sendFile ( byte[] content , byte[] sharedSecret , ArrayList<Integer> choice) throws Exception {
        byte[] encryptedResponse = null;

        if(choice.get(1) == 0){ //AES
            encryptedResponse = AES.encrypt ( content , sharedSecret );
        }else if(choice.get(1) == 1){ //3DES
            //encryptedMessage = 3DES.encrypt ( message.getBytes ( ) , sharedSecret.toByteArray ( ) );
        }
        byte[] digest = null;

        if(choice.get(0) == 0){ //MAC
            // Generates the MAC
            digest = MAC.generateMAC ( content, clientMACKey );
        }else if(choice.get(0) == 1){ //HASH
            digest = Hash.generateDigest(content);
        }
        // Creates the message object
        Message responseObj = new Message ( encryptedResponse , digest );
        // Sends the encrypted message
        out.writeObject ( responseObj );
        out.flush ( );
        // Encrypts the message
        // AES.encrypt ( content , sharedSecret );
        // Generates the MAC
        //byte[] digest = MAC.generateMAC ( content, clientMACKey );
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
        SecretKey macKey = ( SecretKey ) in.readObject ( );
        return macKey;
    }
    private ArrayList<Integer> receiveClientChoice () throws IOException, ClassNotFoundException {
        ArrayList<Integer> choice = (ArrayList<Integer>) in.readObject();
        return choice;
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
        }else if(clientChoice.get(1) == 1){
            //decryptedMessage = 3DES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
        }
        // Computes the digest of the received message
        byte[] computedDigest = null;
        if(clientChoice.get(0) == 0){
            computedDigest = MAC.generateMAC ( decryptedMessage, clientMACKey );
            if ( ! MAC.verifyMAC ( messageObj.getSignature ( ) , computedDigest ) ) {
                throw new RuntimeException ( "The integrity of the message is not verified" );
            }
        }else if (clientChoice.get(0) == 1){
            //decryptedMessage = 3DES.decrypt ( messageObj.getMessage ( ) , sharedSecret );
            computedDigest = Hash.generateDigest(decryptedMessage);
            if ( ! Hash.verifyDigest ( messageObj.getSignature ( ) , computedDigest ) ) {
                throw new RuntimeException ( "The integrity of the message is not verified" );
            }
        }
        // Verifies the integrity of the message

        return decryptedMessage;
    }
}
