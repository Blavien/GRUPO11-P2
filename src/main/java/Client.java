import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.Scanner;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This class represents the client. The client sends the messages to the server by means of a socket. The use of Object
 * streams enables the sender to send any kind of object.
 */
public class Client {
    private static final String HOST = "0.0.0.0";
    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private boolean isConnected;
    private final String userDir;
    private String client_name;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey macKey;
    private int requestLimit;
    private String fileName;
    private PublicKey serverPublicRSAKey;
    private KeyPair clientKeyPair;
    private static final Scanner scan = new Scanner(System.in);
    private BigInteger sharedSecret;


    /**
     * Constructs a Client object by specifying the port to connect to. The socket must be created before the sender can
     * send a message.
     *
     * @param port the port to connect to
     * @throws IOException when an I/O error occurs when creating the socket
     */
    public Client(int port) throws Exception {
        this.requestLimit = 0;

        client = new Socket ( HOST , port );

        out = new ObjectOutputStream ( client.getOutputStream ( ) );

        in = new ObjectInputStream ( client.getInputStream ( ) );

        isConnected = true; // TODO: Check if this is necessary or if it should be controlled

        // Create a temporary directory for putting the request files

        userDir = Files.createTempDirectory ( "fileServer" ).toFile ( ).getAbsolutePath ( );

        System.out.println ( "Temporary directory path " + userDir );

        System.out.println("\nInsert your username");
        String name = scan.next();
        this.client_name = name;

        RSA.storeRSAKeys ( RSA.generateKeyPair() , client_name);

        this.setPrivateKey();
        this.setPublicKey();


    }
    public String getClientName() {
        return client_name;
    }
    public void setClientName(String client_name){
        this.client_name = client_name;
    }
    public void setPrivateKey() throws Exception{ this.privateKey = RSA.getPrivateKey(this.client_name); }
    public void setPublicKey() throws Exception{ this.publicKey = RSA.getPublicKey(this.client_name); }
    public PrivateKey getPrivateKey() throws Exception{ return this.privateKey; }
    public PublicKey getPublicKey() throws Exception{ return RSA.getPublicKey(this.client_name); }
    public void setFileName(String request){
        this.fileName = request;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setConnected(boolean bool) {
        this.isConnected = bool;
    }

    public void doHandshake() throws Exception {
        serverPublicRSAKey = rsaKeyDistribution();
        sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );
        macKey=MAC.createMACKey();
        sendMacKey();
    }

    public void endConnection() throws Exception {
        this.closeConnection();
    }

    /**
     * Reads the response from the server and writes the file to the temporary directory.
     *
     * @param fileName the name of the file to write
     */
    private void processResponse(String fileName) throws Exception {
        String unitedMessage = "";

        //BigInteger sharedSecret = agreeOnSharedSecret(serverPublicRSAKey);

        Message response = (Message) in.readObject();

        byte[] decryptedFile = AES.decrypt(response.getMessage(), sharedSecret.toByteArray());

        byte[] computedDigest = MAC.generateMAC(decryptedFile, macKey);

        if (!Integrity.verifyMAC(response.getSignature(), computedDigest)) {
            throw new RuntimeException("The integrity of the message is not verified");
        }

        String decryptedContent = new String(decryptedFile);  // To handle divided content

        if (decryptedContent.equals("INICIO")) { //Decrypts this first message - INICIO

            //System.out.println("Recebeu o inicio");

            while (!decryptedContent.equals("FIM")){ //Decrypts the rest of the message until we get the last message - FIM

                response = (Message) in.readObject();

                System.out.println(response);

                decryptedFile = AES.decrypt(response.getMessage(), sharedSecret.toByteArray());

                //System.out.println(new String (decryptedFile));

                computedDigest = Integrity.generateMAC(decryptedFile, String.valueOf(macKey));

                if (!Integrity.verifyMAC(response.getSignature(), computedDigest)) {
                    throw new RuntimeException("The integrity of the message is not verified");
                }

                decryptedContent = new String(decryptedFile);

                unitedMessage +=new String(decryptedFile);
            }
            System.out.println("We have reached the end of the file content.");

            System.out.println("This is the file content:");

            System.out.println(new String (unitedMessage));

            saveFiles(unitedMessage);

        }else{
            saveFiles(new String(decryptedFile));
        }
    }

    public void saveFiles(String decryptedFile) throws Exception{
        //Criação da pasta que receberá os ficheiros
        String privateClientPath = this.client_name + "/files";
        File privateClientFolder = new File(privateClientPath);
        privateClientFolder.mkdirs();

        //Criação do ficheiro de texto que receberá o conteúdo do ficheiro de texto pedido e a escrita do mesmo
        String decryptedFileString = decryptedFile;
        File arquivo = new File(this.client_name + "/files/User_" + this.getFileName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo));
        bw.write(decryptedFileString);
        bw.close();
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

    private void sendPublicDHKey ( byte[] publicKey ) throws Exception {
        out.writeObject ( publicKey );
    }

    private PublicKey rsaKeyDistribution ( ) throws Exception {
        // Sends the public key
        sendPublicRSAKey ( );
        // Receive the public key of the sender
        return ( PublicKey ) in.readObject ( );
    }

    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicKey );
        out.flush ( );
    }
    private void sendMacKey() throws Exception{
        out.writeObject(macKey);
        out.flush ();
    }
    public void sendMessage ( String message ) throws Exception {
        // Agree on a shared secret
        // BigInteger sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );
        // Encrypts the message
        byte[] encryptedMessage = AES.encrypt ( message.getBytes ( ) , sharedSecret.toByteArray ( ) );
        // Generates the MAC
        byte[] digest = MAC.generateMAC(message.getBytes ( ),macKey);
        //byte[] digest = Integrity.generateMAC ( message.getBytes ( ), macKey);
        // Creates the message object
        Message messageObj = new Message ( encryptedMessage , digest );
        // Sends the encrypted message
        out.writeObject ( messageObj );
        out.flush();
    }
    private BigInteger agreeOnSharedSecret (PublicKey serverPublicRSAKey ) throws Exception {
        // Generates a private key
        BigInteger privateDHKey = DiffieHellman.generatePrivateKey ( );
        BigInteger publicDHKey = DiffieHellman.generatePublicKey ( privateDHKey );
        // Sends the public key to the server encrypted
        sendPublicDHKey ( RSA.encryptRSA ( publicDHKey.toByteArray ( ) , privateKey ) );
        // Waits for the server to send his public key
        BigInteger serverPublicKey = new BigInteger ( RSA.decryptRSA ( ( byte[] ) in.readObject ( ) , serverPublicRSAKey ) );
        // Generates the shared secret
        return DiffieHellman.computePrivateKey ( serverPublicKey , privateDHKey );
    }

    /**
     * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
     * writes the file to the temporary directory.
     */
    public void execute() throws Exception{
        Scanner usrInput = new Scanner ( System.in );
        if( isConnected ) {
            // Reads the message to extract the path of the file
            System.out.println ( "Write the path of the file" );
            String request = "USERNAME: "+this.client_name+ ": "+usrInput.nextLine ( );
            this.setFileName(RequestUtils.splitRequest(request).get(1));
            // Request the file
            sendMessage ( request );
            // Waits for the response
            processResponse ( RequestUtils.getFileNameFromRequest ( request ) );
        }
    }


}