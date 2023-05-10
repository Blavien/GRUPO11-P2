import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.ArrayList;
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

    private String client_name;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey macKey;
    private int requestLimit;
    private String fileName;
    private PublicKey serverPublicRSAKey;
    private static final Scanner scan = new Scanner(System.in);
    private BigInteger sharedSecret;
    private ArrayList<Integer> choice;       // get(0) - Hashing algorithm       get(1) - Encryption algorithm
    private static String DIGEST_ALGORITHM = "";
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



    public boolean doHandshake() throws Exception {
        boolean handshakeInsuccess = false;
        boolean invalid_choice_hashing = false;
        boolean invalid_choice_encryption = false;
        choice = new ArrayList<Integer>(); //Array novo sempre que isto é chamado

        System.out.println("\nWe will make you set up your commmunication and security, cause I want 20.");

        // choice.get(0)
        System.out.println("\nHashing algoritms:");
        System.out.println("0. SHA-512");
        System.out.println("1. SHA-256");
        System.out.println("2. SHA-1");
        System.out.println("3. MD5");
        System.out.println("4. 2048 eggs & bacon");
        System.out.println("5. EFFICIENCY IS OVERRATED");
        int i = scan.nextInt();
        System.out.println(i);

        System.out.println("\nEncryption/Decryption algoritm:\n");
        System.out.println("0. AES\n");
        System.out.println("1. DES\n");
        System.out.println("2. 3DES\n");
        System.out.println("3. 360-no-scope-DEES\n");

        int v = scan.nextInt();
        System.out.println(v);

        switch (i){
            case 0:
                DIGEST_ALGORITHM = "HmacSHA512";
                choice.add(0); // [0] = 0
                break;
            case 1:
                DIGEST_ALGORITHM = "HmacSHA256";
                choice.add(1); // [0] = 1
                break;
            case 2:
                DIGEST_ALGORITHM = "HmacSHA1";
                choice.add(2); // [0] = 2
                break;
            case 3:
                DIGEST_ALGORITHM = "HmacMD5";
                choice.add(3); // [0] = 3
                break;
            case 4:
                invalid_choice_hashing = true;
                break;
            case 5:
                invalid_choice_hashing = true;
                break;
        }
        // choice.get(1)

        switch (v){


            case 0:
                choice.add(0); // [1] = 0
                break;
            case 1:
                choice.add(1); // [1] = 1
                break;
            case 2:
                choice.add(2); // [1] = 2
                break;
            case 3:
                invalid_choice_encryption = true;
                break;
        }

        //Faz o handshake
        if(invalid_choice_hashing == false && invalid_choice_encryption == false){
            serverPublicRSAKey = rsaKeyDistribution();

            sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );

            this.macKey = Hmac.createMACKey(DIGEST_ALGORITHM);

            //System.out.println(choice);
            sendClientChoice();

            sendMacKey();

        }else{
            handshakeInsuccess = true;
            System.out.println("This server doesn't support those algorithms.");
        }
        return handshakeInsuccess;
    }

    /**
     * Send teh array with the client's security set up, hashing and encryption/decryption algorithms
     * @throws IOException
     */
    public void sendClientChoice() throws IOException {
        out.writeObject(choice);
        out.flush ();
    }

    /**
     *  useless function, but it's to call this client's closeConnection() method
     * @throws Exception
     */
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
        byte[] decryptedFile = null;
        //CRYPTO
        switch (choice.get(1)){
            case 0:
                decryptedFile = AES.decrypt(response.getMessage(), sharedSecret.toByteArray());
                break;
            case 1:
                decryptedFile = DES.decrypt(response.getMessage(), sharedSecret.toByteArray());
                break;
            case 2:
                decryptedFile = DES3.decrypt(response.getMessage(), sharedSecret.toByteArray());
                break;
        }


        //HASHING
        byte[] computedDigest = Hmac.hmac(decryptedFile,DIGEST_ALGORITHM, macKey.getEncoded());
        if (!Hmac.verifyDigest(response.getSignature(), computedDigest)) {
                throw new RuntimeException("The integrity of the message is not verified");

        }

        String decryptedContent = new String(decryptedFile);  // To handle divided content

        if (decryptedContent.equals("INICIO")) { //Decrypts this first message - INICIO

            while (!decryptedContent.equals("FIM")){ //Decrypts the rest of the message until we get the last message - FIM

                response = (Message) in.readObject();
                //CRYPTO $$
                switch (choice.get(1)){
                    case 0:
                        decryptedFile = AES.decrypt(response.getMessage(), sharedSecret.toByteArray());
                        break;
                    case 1:
                        decryptedFile = DES.decrypt(response.getMessage(), sharedSecret.toByteArray());
                        break;
                    case 2:
                        decryptedFile = DES3.decrypt(response.getMessage(), sharedSecret.toByteArray());
                        break;
                }

                //HASHING
                computedDigest = Hmac.hmac(decryptedFile,DIGEST_ALGORITHM, macKey.getEncoded());
                if (!Hmac.verifyDigest(response.getSignature(), computedDigest)) {
                    throw new RuntimeException("The integrity of the message is not verified");
                }

                decryptedContent = new String(decryptedFile);

                if(!decryptedContent.equals("FIM")) {
                    unitedMessage += new String(decryptedFile);
                }
            }
            System.out.println("We have reached the end of the file content.");

            System.out.println("This is the file content:");

            System.out.println(new String (unitedMessage));
            saveFiles(unitedMessage);

        }else{
            if(!decryptedContent.equals("FIM")) {
                System.out.println("\nThis is the file content:");

                System.out.println(new String (decryptedFile));

                saveFiles(new String(decryptedFile));
            }
        }
    }

    /**
     *
     * @param decryptedFile File that has been sent by the server
     * @throws Exception exception
     */
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

    /**
     *
     * @param publicKey Client's DH public key
     * @throws Exception exception
     */
    private void sendPublicDHKey ( byte[] publicKey ) throws Exception {
        out.writeObject ( publicKey );
    }

    /**
     *
     * @return  Returns the server key
     * @throws Exception exception
     */
    private PublicKey rsaKeyDistribution ( ) throws Exception {
        // Sends the public key
        sendPublicRSAKey ( );
        // Receive the public key of the sender
        return ( PublicKey ) in.readObject ( );
    }

    /**
     * Sends RSA public Key
     * @throws IOException ioexception
     */
    private void sendPublicRSAKey ( ) throws IOException {
        out.writeObject ( publicKey );
        out.flush ( );
    }

    /**
     *  Sends the client Mac Key to the server, so it can verify the integrity and authenticity
     * @throws Exception
     */
    private void sendMacKey() throws Exception{
        out.writeObject(macKey);
        out.flush ();
    }

    /**
     *
     * @param message Message being sent to the server
     * @throws Exception If any error occurs during this function it will throw an exception
     */
    public void sendMessage ( String message ) throws Exception {
        // Agree on a shared secret
        // BigInteger sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );
        // Encrypts the message
        byte[] encryptedMessage = null;
        switch (choice.get(1)){
            case 0:
                encryptedMessage = AES.encrypt ( message.getBytes ( ) , sharedSecret.toByteArray ( ) );
                break;
            case 1:
                encryptedMessage = DES.encrypt ( message.getBytes ( ) , sharedSecret.toByteArray ( ) );
                break;
            case 2:
                encryptedMessage = DES3.encrypt(message.getBytes(), sharedSecret.toByteArray());
                break;
        }
        byte[] digest = Hmac.hmac(message.getBytes ( ), DIGEST_ALGORITHM,macKey.getEncoded());

        // Creates the message object
        Message messageObj = new Message ( encryptedMessage , digest );
        // Sends the encrypted message
        out.writeObject ( messageObj );
        out.flush();

    }

    /**
     * @param serverPublicRSAKey    Server's key that was shared though the handshake
     * @return  a shared shecret, that is going to be used to encrypt & decrypt
     * @throws Exception exception
     */
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
    public void execute(String test) throws Exception{
        Scanner usrInput = new Scanner ( System.in );
        if( isConnected ) {
            String request="";
            // Reads the message to extract the path of the file
            System.out.println ( "Write the path of the file" );
            if(test!=""){
                request = "USERNAME: "+this.client_name+ ": GET : "+ test ;
            }
            if(test==""){
            request = "USERNAME: "+this.client_name+ ": "+usrInput.nextLine ( );
            }
            this.setFileName(RequestUtils.splitRequest(request).get(1));
            // Request the file
            sendMessage ( request );
            // Waits for the response
            processResponse ( RequestUtils.getFileNameFromRequest ( request ) );
        }
    }

    /**
     *
     * @return this client's name
     */
    public String getClient_name() {
        return client_name;
    }

    /**
     * Sets this
     * @param client_name client name
     */
    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }


}