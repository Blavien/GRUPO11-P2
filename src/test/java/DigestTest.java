import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigestTest {

    @DisplayName("Test the generate Digest")
    @Test
    void generateDigest() throws Exception {
        String message = "Hello, world!";
        byte[] expectedDigest = {-46, 59, -39, 3, 7, 8, -16, 3, -10, -47, 57, -103, -105, 16, 109, -13, -97, -42, 118, 63, -28, -33, -62, -75, -30, -39, 25, -52, -99, 39, -40, -37, -102, -45, -83, 82, 83, -83, 46, -13, 19, -46, 42, 98, -33, 30, 55, -8, 76, -114, -12, 24, 71, -72, -63, -29, -40, -98};
        byte[] actualDigest = Digest.generateDigest(message.getBytes());
        assertArrayEquals(expectedDigest, actualDigest);

    }

    @DisplayName("Test the generate Digest")
    @Test
    void verifyDigest() {

    }
}