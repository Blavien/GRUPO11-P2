import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class DES3 {
    //É SUPOST SER 3DES MAS NÃO ME DEIXA CHAMAR 3DES
    public static byte[] encrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 24); // 3DES key size is 24 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }

    public static byte[] decrypt(byte[] message, byte[] secretKey) throws Exception {
        byte[] secretKeyPadded = Arrays.copyOf(secretKey, 24); // 3DES key size is 24 bytes
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyPadded, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(message);
    }

}
