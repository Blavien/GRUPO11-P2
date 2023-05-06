import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
   /**
    * Constructs a Client object by specifying the port to connect to. The socket must be created before the sender can
    * send a message.
    *
    * @param port the port to connect to
    *
    * @throws IOException when an I/O error occurs when creating the socket
    */
   public Client ( int port ) throws Exception {
      this.requestLimit = 0;
      client = new Socket ( HOST , port );

      out = new ObjectOutputStream ( client.getOutputStream ( ) );
      in = new ObjectInputStream ( client.getInputStream ( ) );
      isConnected = true; // TODO: Check if this is necessary or if it should be controlled
      // Create a temporary directory for putting the request files
      userDir = Files.createTempDirectory ( "fileServer" ).toFile ( ).getAbsolutePath ( );
      System.out.println ( "Temporary directory path " + userDir );

       KeyPair clientKeyPair = RSA.generateKeyPair();
       RSA.storeRSAKeys ( clientKeyPair,this.client_name);

       this.setPrivateKey();
       this.setPublicKey();

       serverPublicRSAKey = rsaKeyDistribution ( );
   }
   /*a*/
   public boolean isConnected() {
       return isConnected;
   }
   public String getClientName() {
       return client_name;
   }
   public void setClientName(String client_name){
       this.client_name = client_name;
   }
   public void setPrivateKey() throws Exception{
       this.privateKey = RSA.getPrivateKey(this.client_name);
   }
   public void setPublicKey() throws Exception{
       this.publicKey = RSA.getPublicKey(this.client_name);
   }

   public PrivateKey getPrivateKey() throws Exception{
       return this.privateKey;
   }
   public PublicKey getPublicKey() throws Exception{
       return RSA.getPublicKey(this.client_name);
   }
    public void setFileName(String request){
        this.fileName = request;
    }
    public String getFileName(){
        return this.fileName;
    }
    public void setConnected(boolean bool){
       this.isConnected = bool;
    }
   public boolean reachedLimit() throws Exception {
       requestLimit = getRequestLimit(); //Busca o número de requests guardados no ficheiroSystem.out.println(requestLimit);
       if(requestLimit >= 5 ){
           return true;
       }else{
           return false;
       }
   }
   public int getRequestLimit() throws Exception{
       return RequestUtils.getRequestLimit(this.client_name);
   }
   /**
    * Reads the response from the server and writes the file to the temporary directory.
    *
    * @param fileName the name of the file to write
    */
   private void processResponse ( String fileName ) {
       try {
           Message response = ( Message ) in.readObject ( );
           System.out.println ( "File received" );
           //FileHandler.writeFile ( userDir + "/" +  this.getFileName() , response.getMessage ( ) );
       } catch ( IOException | ClassNotFoundException e ) {
           e.printStackTrace ( );
       }
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
        System.out.println("CLIENT STARTED THE SEND");
        sendPublicRSAKey ( );
        System.out.println("SEND CLIENT KEY");
        // Receive the public key of the sender
        System.out.println("READ SERVER KEY");
        return ( PublicKey ) in.readObject ( );
    }

    private void sendPublicRSAKey ( ) throws IOException {
        System.out.println("CLIENT SENDING THEM PUK : ");
        out.writeObject ( publicKey );
        out.flush ( );
    }

    public void sendMessage ( String message ) throws Exception {
        // Agree on a shared secret
        BigInteger sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );
        // Encrypts the message
        byte[] encryptedMessage = AES.encrypt ( message.getBytes ( ) , sharedSecret.toByteArray ( ) );
        // Generates the MAC
        byte[] digest = Integrity.generateDigest ( message.getBytes ( ) );
        // Creates the message object
        Message messageObj = new Message ( encryptedMessage , digest );
        // Sends the encrypted message
        out.writeObject ( messageObj );
        out.flush();
    }
    public void teste() throws Exception {
        BigInteger sharedSecret = agreeOnSharedSecret ( serverPublicRSAKey );
        String messages="Teste";
        byte[] encryptedMessages = AES.encrypt ( messages.getBytes ( ) , sharedSecret.toByteArray ( ) );
        System.out.println(encryptedMessages);
        byte[] decryptdMessages = AES.decrypt ( messages.getBytes ( ) , sharedSecret.toByteArray ( ) );
        System.out.println(decryptdMessages);
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
