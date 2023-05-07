import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This class implements the generation and verification of the message digest.
 */
public class Integrity {

    private static final String MAC_ALGORITHM = "HmacSHA256";

    /**
     * Computes the message digest of the given message.
     *
     * @param message The message to be digested.
     *
     * @return the message digest
     *
     * @throws Exception if the message digest algorithm is not available
     */
    public static byte[] generateMAC ( byte[] message , String macKey ) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec ( macKey.getBytes ( ) , MAC_ALGORITHM );
        Mac mac = Mac.getInstance ( MAC_ALGORITHM );
        mac.init ( secretKeySpec );
        return mac.doFinal ( message );
    }

    /**
     * Verifies the message digest of the given message.
     *
     * @param digest         the message digest to be verified
     * @param computedDigest the computed message digest
     *
     * @return true if the message digest is valid, false otherwise
     */
    public static boolean verifyMAC ( byte[] mac , byte[] computedMac ) {
        return Arrays.equals ( mac , computedMac );
    }

}