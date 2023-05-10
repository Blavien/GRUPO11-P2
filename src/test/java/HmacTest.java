import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class HmacTest {

    private static final String DIGEST_ALGORITHM = "HmacSHA256";
    private static SecretKey key;
    private static byte[] message;

    @BeforeAll
    static void setUp() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(DIGEST_ALGORITHM);
        SecureRandom random = new SecureRandom();
        keyGen.init(random);
        key = keyGen.generateKey();
        message = "message".getBytes();

    }

    @Test
    @DisplayName("Test if Mac is created")
    void creatMAC() {
        try {
            String DIGEST_ALGORITHM = "HmacSHA256";
            KeyGenerator keyGen = KeyGenerator.getInstance(DIGEST_ALGORITHM);
            int keySize = 256; // in bits
            keyGen.init(keySize);
            SecretKey secretKey = Hmac.createMACKey(DIGEST_ALGORITHM);
            assertEquals(DIGEST_ALGORITHM, secretKey.getAlgorithm());
            assertEquals(keySize/8, secretKey.getEncoded().length);
            assertNotNull(key);
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    @DisplayName("Test the Verify Digest")
    void testVerifyDigest() {
        try {
            byte[] digest = Hmac.hmac(message, DIGEST_ALGORITHM, key.getEncoded());
            assertTrue(Hmac.verifyDigest(digest, Hmac.hmac(message, DIGEST_ALGORITHM, key.getEncoded())));
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }
    @Test
    @DisplayName("Test if the message digest is not verified when the digests are different")
    void testVerifyDigestFails() {
        try {
            byte[] digest = Hmac.hmac(message, DIGEST_ALGORITHM, key.getEncoded());
            message="mudanças cuidado".getBytes();

            assertFalse(Hmac.verifyDigest(digest, Hmac.hmac(message, DIGEST_ALGORITHM, key.getEncoded())));
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    @DisplayName("Test if the message is correctly hashed")
    void testHmac() {
        try {
            byte[] digest = Hmac.hmac(message, DIGEST_ALGORITHM, key.getEncoded());
            assertNotNull(digest);
            assertTrue(digest.length > 0);
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

}