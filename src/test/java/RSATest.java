import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RSATest {

   private static PublicKey publicKey;
   private static PrivateKey privateKey;
   private static  KeyPair keyPair;
   private   final static String clientName="Nome";
    private RSA Crypto;

    @BeforeAll
    @DisplayName("Conffigure the setuo to teste the things")
    public static void setup() throws Exception {
        keyPair = RSA.generateKeyPair();
         RSA.storeRSAKeys(keyPair,clientName);
        publicKey = RSA.getPublicKey(clientName);
        privateKey = RSA.getPrivateKey(clientName);

    }
    @Test
    @DisplayName("Test the Encrption and Descryption")
    void testEncryptionAndDecryption() throws Exception {
        String message = "Message";
        KeyPair keyPair = RSA.generateKeyPair();
        byte[] encryptedMessage = RSA.encryptRSA(message.getBytes(), keyPair.getPublic());
        byte[] decryptedMessage = RSA.decryptRSA(encryptedMessage, keyPair.getPrivate());


        assertNotEquals(message,new String(encryptedMessage));
        assertEquals(message, new String(decryptedMessage));
    }

    @Test
    @DisplayName("Test if exist Keypair")
    void testKeyPairGeneration() throws Exception {
        KeyPair keyPair = RSA.generateKeyPair();
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }

    @Test
    @DisplayName("Test if exist Public Key")
    void testPublicKeyRetrieval() throws Exception {
        RSA.storeRSAKeys(keyPair, clientName);
        assertNotNull(publicKey);
    }

    @Test
    @DisplayName("Test if exist Private Key")
    void testPrivateKeyRetrieval() throws Exception {
        RSA.storeRSAKeys(keyPair, clientName);
        assertNotNull(privateKey);
    }



    @Test
    @DisplayName("Test if the Message is Invalid")
    void testInvalidMessage() throws Exception {
        byte[] secretKey = "secret001".getBytes();
        byte[] message = "Ol áMundo".getBytes();

        byte[] encryptedMessage = RSA.encryptMessage(message, secretKey);
        byte[] modifiedEncryptedMessage = Arrays.copyOf(encryptedMessage, encryptedMessage.length);
        modifiedEncryptedMessage[0] = (byte) ~modifiedEncryptedMessage[0];

        assertThrows(BadPaddingException.class, () -> RSA.decryptMessage(modifiedEncryptedMessage, secretKey));
    }



}