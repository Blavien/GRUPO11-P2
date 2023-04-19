import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * This class implements the generation and verification of the message authentication code (MAC).
 */
public class Integrity {

    private static final String MAC_ALGORITHM = "HmacSHA256";

    /**
     * Generates the message authentication code (MAC) of the message.
     *
     * @return the message authentication code
     *
     * @throws Exception when the MAC generation fails
     */
    public static byte[] generateMAC ( byte[] message , String macKey ) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec ( macKey.getBytes ( ) , MAC_ALGORITHM );
        Mac mac = Mac.getInstance ( MAC_ALGORITHM );
        mac.init ( secretKeySpec );
        return mac.doFinal ( message );
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