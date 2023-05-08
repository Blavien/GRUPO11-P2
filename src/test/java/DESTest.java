import org.junit.Test;
import static org.junit.Assert.*;

public class DESTest {

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        byte[] message = "Estudasses!".getBytes();
        byte[] secretKey = "se123".getBytes();

        byte[] encryptedMessage = DES.encrypt(message, secretKey);
        byte[] decryptedMessage = DES.decrypt(encryptedMessage, secretKey);

        assertArrayEquals(message, decryptedMessage);
    }

}
