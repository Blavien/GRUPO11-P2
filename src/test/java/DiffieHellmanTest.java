import org.junit.Test;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DiffieHellmanTest {

    @Test
    public void testGeneratePrivateKey() throws NoSuchAlgorithmException {
        BigInteger privateKey1 = DiffieHellman.generatePrivateKey();
        BigInteger privateKey2 = DiffieHellman.generatePrivateKey();

        assertNotEquals(privateKey1, privateKey2);
    }

    @Test
    public void testGeneratePublicKey() throws NoSuchAlgorithmException {
        BigInteger privateKey = DiffieHellman.generatePrivateKey();

        BigInteger publicKey = DiffieHellman.generatePublicKey(privateKey);

        assertTrue(publicKey.compareTo(BigInteger.ZERO) > 0);
    }

    @Test
    public void testComputePrivateKey() throws NoSuchAlgorithmException {
        BigInteger privateKey1 = DiffieHellman.generatePrivateKey();
        BigInteger privateKey2 = DiffieHellman.generatePrivateKey();

        BigInteger publicKey1 = DiffieHellman.generatePublicKey(privateKey1);
        BigInteger publicKey2 = DiffieHellman.generatePublicKey(privateKey2);

        BigInteger sharedSecret1 = DiffieHellman.computePrivateKey(publicKey2, privateKey1);
        BigInteger sharedSecret2 = DiffieHellman.computePrivateKey(publicKey1, privateKey2);

        assertEquals(sharedSecret1, sharedSecret2);
    }

}
