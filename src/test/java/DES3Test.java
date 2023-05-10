import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DES3Test {

    @Test
    void testEncryptDecrypt() throws Exception {
        byte[] message = "Hello World".getBytes();
        byte[] secretKey = "MySecretKey1234567890".getBytes();

        // Encrypt and decrypt using DES3
        byte[] encryptedMessage = DES3.encrypt(message, secretKey);
        byte[] decryptedMessage = DES3.decrypt(encryptedMessage, secretKey);

        // Check that the decrypted message matches the original message
        Assertions.assertArrayEquals(message, decryptedMessage);
    }

}