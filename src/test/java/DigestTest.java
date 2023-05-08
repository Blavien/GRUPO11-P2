import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigestTest {





    @DisplayName("Test the generate Digest")
    @Test
    void generateDigest() throws Exception {
        String message = "a";
        byte[] actualDigest = Digest.generateDigest(message.getBytes());
        assertNotNull( actualDigest);

    }

    @DisplayName("Test the generate Digest")
    @Test
    void verifyDigest() throws Exception {
        String message = "Cogumelos mar e sol";
        byte[]  firstDigest = Digest.generateDigest(message.getBytes());
        byte[]  secondDigest = Digest.generateDigest("Test".getBytes());
        assertFalse(Digest.verifyDigest(firstDigest, secondDigest));
    }
}