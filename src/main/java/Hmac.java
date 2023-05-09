import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class implements the generation and verification of the message digest.
 */
public class Hmac {

    public static SecretKey createMACKey (String DIGEST_ALGORITHM) throws Exception{
        KeyGenerator keyGen = KeyGenerator.getInstance(DIGEST_ALGORITHM);
        SecureRandom random = new SecureRandom();
        keyGen.init(random);
        SecretKey macKey = keyGen.generateKey();
        return macKey;
    }

    /**
     * Computes the message digest of the given message.
     *
     * @param message The message to be digested.
     *
     * @return the message digest
     *
     * @throws Exception if the message digest algorithm is not available
     */
    public static byte[] generateDigest ( byte[] message, String DIGEST_ALGORITHM) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance ( DIGEST_ALGORITHM );
        return messageDigest.digest ( message );
    }

    /**
     * Verifies the message digest of the given message.
     *
     * @param digest         the message digest to be verified
     * @param computedDigest the computed message digest
     *
     * @return true if the message digest is valid, false otherwise
     */
    public static boolean verifyDigest ( byte[] digest , byte[] computedDigest ) {
        return Arrays.equals ( digest , computedDigest );
    }

    public static byte[] hmac (byte[] message, String DIGEST_ALGORITHM, byte[] key)throws Exception{
        SecretKey secretKeySpec = new SecretKeySpec(key , DIGEST_ALGORITHM);
        Mac mac = Mac.getInstance(DIGEST_ALGORITHM);
        mac.init(secretKeySpec);
        return mac.doFinal( message );
    }

}