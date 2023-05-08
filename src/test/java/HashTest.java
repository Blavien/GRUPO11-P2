import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.Arrays;

public class HashTest {

    @Test
    void testGenerateDigest() throws Exception {
        // Test with empty message
        byte[] message = new byte[0];
        byte[] digest = Hash.generateDigest(message, "SHA-256");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] expectedDigest = md.digest(message);
        Assertions.assertArrayEquals(expectedDigest, digest);

        // Test with non-empty message
        message = "Hello World".getBytes();
        digest = Hash.generateDigest(message, "MD5");
        md = MessageDigest.getInstance("MD5");
        expectedDigest = md.digest(message);
        Assertions.assertArrayEquals(expectedDigest, digest);
    }

    @Test
    void testVerifyDigest() {
        // Test with valid digest
        byte[] digest = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3".getBytes();
        byte[] computedDigest = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3".getBytes();
        Assertions.assertTrue(Hash.verifyDigest(digest, computedDigest));

        // Test with invalid digest
        digest = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3".getBytes();
        computedDigest = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd4".getBytes();
        Assertions.assertFalse(Hash.verifyDigest(digest, computedDigest));
    }

}
