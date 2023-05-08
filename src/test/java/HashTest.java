import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class HashTest {
    private String message ="test message";
    private byte[] messageBytes = message.getBytes();
    @Test
    public void testGenerateDigest() {
        try {
            byte[] digest = Hash.generateDigest(messageBytes);
            Assertions.assertNotNull(digest);
            Assertions.assertTrue(digest.length > 0);
        } catch (Exception e) {
            Assertions.fail("An exception occurred while generating the digest: " + e.getMessage());
        }
    }

    @Test
    public void testVerifyDigest() {
        try {
            byte[] digest = Hash.generateDigest(messageBytes);
            boolean isValid = Hash.verifyDigest(digest, digest);
            Assertions.assertTrue(isValid);
        } catch (Exception e) {
            Assertions.fail("Error " + e.getMessage());
        }
    }

    @Test
    public void testVerifyDigestDifferent() {
        byte[] messageBytes = message.getBytes();
        try {
            byte[] digest = Hash.generateDigest(messageBytes);
            byte[] differentDigest = Arrays.copyOf(digest, digest.length);
            differentDigest[differentDigest.length - 1] = (byte) ~differentDigest[differentDigest.length - 1]; // invert last byte
            boolean isValid = Hash.verifyDigest(digest, differentDigest);
            Assertions.assertFalse(isValid);
        } catch (Exception e) {
            Assertions.fail("Error" + e.getMessage());
        }
    }
}
