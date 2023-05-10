import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.Arrays;

public class DES {
    /**
     * @param message   the message to be encrypted
     * @param secretKey the secret key used to encrypt the message
     *
     * @return the encrypted message as an array of bytes
     *
     * @throws Exception when the decryption fails
     */
    public static byte[] encrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 8); // DES key size is 8 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }
    /**
     * @param message   the message to be decrypted
     * @param secretKey the secret key used to decrypt the message
     *
     * @return the decrypted message as an array of bytes
     *
     * @throws Exception when the encryption fails
     */
    public static byte[] decrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 8); // DES key size is 8 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }


}
