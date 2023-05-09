import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class AESTest {


    private AES Crypto;


        private final byte[] secretKey = "secretkey012".getBytes(StandardCharsets.UTF_8);
        private final byte[] message = "mmessage".getBytes(StandardCharsets.UTF_8);


        @Test
        @DisplayName("Test is the message is encrypted and descrypted")
        void testEncryptDecrypt() throws Exception {

            byte[] encrypted = Crypto.encrypt(message, secretKey);
            byte[] decrypted = Crypto.decrypt(encrypted, secretKey);
            assertNotEquals(message, encrypted);

        }

}