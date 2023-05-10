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


        @Test
        @DisplayName("Test is the message is encrypted and descrypted")
        void testEncryptDecrypt() throws Exception {
            byte[] secretKey = "secretkey012".getBytes();
            byte[] message = "message".getBytes();
            byte[] encrypted = AES.encrypt(message, secretKey);
            byte[] desincrypted = AES.decrypt(encrypted,secretKey);
            String decryptedString = new String(desincrypted);
            String messageString = new String(message);
                    assertNotEquals(message, encrypted);
                    assertEquals(messageString,decryptedString);

        }

}