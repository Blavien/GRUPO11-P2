import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MACTest {

    private static final String MESSAGE = "Test ";
    private static final String OTHER_MESSAGE = "Other TEST";
    private static SecretKey key;
    private static byte[] mac;

    @BeforeAll
    @DisplayName("Conffigure the setuo to test the MAC")
    public static void setup() throws Exception {
         key = MAC.createMACKey();
         mac = MAC.generateMAC(MESSAGE.getBytes(), key);

    }
    @Test
    @DisplayName(" generating a MAC and verifying it")
    void testGenerateAndVerifyMAC() throws Exception {
        
  
        assertTrue(MAC.verifyMAC(mac, MAC.generateMAC(MESSAGE.getBytes(), key)));
        assertFalse(MAC.verifyMAC(mac, MAC.generateMAC(OTHER_MESSAGE.getBytes(), key)));
    }
    @Test
    @DisplayName("creating a MAC key")
    void testCreateMACKey() throws Exception {
        assertNotNull(key);
    }


    @Test
    @DisplayName("Test verifying an invalid MAC")
    void testVerifyInvalidMAC() throws Exception {
 
        byte[] modifiedMac = Arrays.copyOf(mac, mac.length);
        modifiedMac[0] = (byte) ~modifiedMac[0];
        assertFalse(MAC.verifyMAC(mac, modifiedMac));
    }

}
