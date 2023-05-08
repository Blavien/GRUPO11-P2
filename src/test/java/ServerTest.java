import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.security.*;

public class ServerTest {

    private static int PORT = 8081;
    private Thread serverThread;
    private Socket client;
    private PrivateKey privateRSAKey;
    private PublicKey publicRSAKey;
    private ServerSocket server;


    @BeforeEach
    void setUp() throws Exception {
        // Fecha o socket se já estiver aberto
        try {

             server = new ServerSocket ( PORT );
            KeyPair keyPair = RSA.generateKeyPair();
            this.privateRSAKey = keyPair.getPrivate();
            this.publicRSAKey = keyPair.getPublic();
            RSA.storeRSAKeys(keyPair,"Server_G11");
            String server_name = "Server_G11";
        } catch (IOException e) {
            // Ignora exceção se o socket não estiver aberto
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        serverThread.interrupt();
    }

    /*@Test
    void testGetPrivateRSAKeyandPublicRSAkey() throws Exception {
        PrivateKey privateKey = Server.getPrivateRSAKey();
        assertNotNull(privateKey);
        PublicKey publicKey = Server.getPublicRSAKey();
        assertNotNull(publicKey);
    }
*/
/*

    @Test
    void testProcess() throws Exception {
        Socket client = server.accept ( );
        ClientHandler clientHandler = new ClientHandler(server.accept());
        clientHandler.start();
         String response="teste";
            assertEquals("received test message", response);

        client.close();
    }
*/
}
