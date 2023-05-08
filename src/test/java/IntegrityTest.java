import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class IntegrityTest {
    private final String MAC_KEY="Mas2142SS!±";
    @Test
    public void testGenerateMAC() throws Exception {
        byte[] message = "Tranquilo ".getBytes();
        byte[] computedMac = Integrity.generateMAC(message, MAC_KEY);
        Assertions.assertNotNull(computedMac);
    }

    @Test
    public void testVerifyMAC() {
        byte[] mac = {-9, -75, -44, 42, 44, -60, 41, 81, -25, 67, -121, -32, 58, -84, 29, -67, 98, -93, -24, -43, 117, -68, 89, 92, -11, 2, 31, -69, 123, -59, -111, -78};
        byte[] computedMac=mac;
        Assertions.assertTrue(Integrity.verifyMAC(mac, computedMac));
    }

    @Test
    public void testVerifyMACInvalid() {
        byte[] mac = {-9, -75, -44, 42, 44, -60, 41, 81, -25, 67, -121, -32, 58, -84, 29, -67, 98, -93, -24, -43, 117, -68, 89, 92, -11, 2, 31, -69, 123, -59, -111, -78};
        byte[] computedMac = {-1, -71, -41, 42, 44, -60, 41, 11, -15, 17, -121, -32, 18, -24, 59, -87, 98, -93, -24, -43, 117, -68, 89, 92, -11, 2, 31, -69, 123, -59, -111, -79};
        Assertions.assertFalse(Integrity.verifyMAC(mac, computedMac));
    }

}
