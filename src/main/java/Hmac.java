import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
        return keyGen.generateKey();
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