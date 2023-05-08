import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class implements the generation and verification of the message authentication code (MAC).
 */
public class MAC {
    private static final String MAC_ALGORITHM = "HmacSHA256";

    public static SecretKey createMACKey () throws Exception{
        KeyGenerator keyGen = KeyGenerator.getInstance(MAC_ALGORITHM);
        SecureRandom random = new SecureRandom();
        keyGen.init(random);
        SecretKey macKey = keyGen.generateKey();
        return macKey;
    }
    /**
     * Generates the message authentication code (MAC) of the message.
     *
     * @return the message authentication code
     *
     * @throws Exception when the MAC generation fails
     */
    public static byte[] generateMAC (byte[] message, Key key) throws Exception {
        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(key);
        return mac.doFinal(message);
    }

    /**
     * Verifies the message authentication code (MAC) of the message.
     *
     * @param mac         the message authentication code
     * @param computedMac the computed message authentication code
     *
     * @return true if the message authentication codes are equal, false otherwise
     */
    public static boolean verifyMAC ( byte[] mac , byte[] computedMac ) {
        return Arrays.equals ( mac , computedMac );
    }
}