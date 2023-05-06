import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.Scanner;
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
   private final boolean isConnected;
   private final String userDir;
   private String client_name;
   private PrivateKey privateKey;
   private PublicKey publicKey;
   private SecretKey macKey;
   private int requestLimit;

   private String fileName;
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

       //Generate unique macKey for this client
       macKey = MAC.createMACKey();

   }

    public boolean reachedLimit() throws IOException {
       requestLimit = RequestUtils.requestLimit(this.client_name); //Busca o número de requests guardados no ficheiroSystem.out.println(requestLimit);
        if(requestLimit >= 5 ){
            return true;
        }else{
            return false;
        }
    }
    public int getRequestLimit() throws Exception{
       return RequestUtils.requestLimit(this.client_name);
    }

    public SecretKey getMacKey(){
       return this.macKey;
   }
   public void setMacKey() throws Exception{
       this.macKey = MAC.createMACKey();;
   }
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
   public KeyPair initClient() throws Exception {
       KeyPair clientKeyPair = RSA.generateKeyPair();
       RSA.storeRSAKeys ( clientKeyPair,this.client_name);

       return clientKeyPair;
   }
   /**
    * Executes the client. It reads the file from the console and sends it to the server. It waits for the response and
    * writes the file to the temporary directory.
    */

   public void execute ( ) {
           //Start the handshake here

           //Then i can request files

   }
    public void request() throws Exception{
       Scanner usrInput = new Scanner ( System.in );
       if( isConnected ) {
           // Reads the message to extract the path of the file
           System.out.println ( "Write the path of the file" );
           String request = "USERNAME: "+this.client_name+ ": "+usrInput.nextLine ( );
           this.setFileName(RequestUtils.splitRequest(request).get(1));
           //CONCLUIR A CHAVE
           //Encriptar o pedido do cliente
          // AES.encrypt(request.getBytes(),);
           // Request the file
           sendMessage ( request );
           // Waits for the response
           processResponse ( RequestUtils.getFileNameFromRequest ( request ) );

       }
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
           //TERMINAR ISTO
           String Folder = client_name + "/files/" + this.getFileName();
           File userFolder = new File(Folder);
           if (!userFolder.exists()) {
               userFolder.mkdirs();
           }

           // Desencripta a resposta do servidor
           //String x= AES.decrypt(response.getMessage(),xxx);
           //Guarda a resposta do servidor em um ficheiro à parte
           FileHandler.writeFile ( this.client_name + "/files/" + this.getFileName() , x);
           //Print da resposta na consola
           System.out.println(x);
       } catch ( IOException | ClassNotFoundException e ) {
           e.printStackTrace ( );
       }
   }

   /**
    * Sends the path of the file to the server using the OutputStream of the socket. The message is sent as an object
    * of the {@link Message} class.
    *
    * @param filePath the message to send
    *
    * @throws IOException when an I/O error occurs when sending the message
    */
   public void sendMessage ( String filePath ) throws IOException {
       // Creates the message object
       Message messageObj = new Message ( filePath.getBytes ( ) );
       // Sends the message
       out.writeObject ( messageObj );
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

    public void setFileName(String request){
        this.fileName = request;
    }
    public String getFileName(){
        return this.fileName;
    }

}
