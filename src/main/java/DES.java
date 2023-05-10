import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.Arrays;

public class DES {

    public static byte[] encrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 8); // DES key size is 8 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }

    public static byte[] decrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 8); // DES key size is 8 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }


}
