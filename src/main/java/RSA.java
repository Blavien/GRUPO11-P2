import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.io.*;

public class RSA {

    public static PrivateKey getPrivateKey(String client_name) throws Exception{
        String privateKeyFolder = client_name + "/private";
        FileInputStream fis = new FileInputStream(privateKeyFolder + "/privateKey.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PrivateKey privateKey = (PrivateKey) ois.readObject();
        ois.close();
        return privateKey;
    }
    public static byte[] decryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        byte[] secretKeyPadded = ByteBuffer.allocate ( 16 ).put ( secretKey ).array ( );
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKeyPadded , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.DECRYPT_MODE , secreteKeySpec );
        return cipher.doFinal ( message );
    }
    public static byte[] encryptMessage ( byte[] message , byte[] secretKey ) throws Exception {
        byte[] secretKeyPadded = ByteBuffer.allocate ( 16 ).put ( secretKey ).array ( );
        SecretKeySpec secreteKeySpec = new SecretKeySpec ( secretKeyPadded , "AES" );
        Cipher cipher = Cipher.getInstance ( "AES/ECB/PKCS5Padding" );
        cipher.init ( Cipher.ENCRYPT_MODE , secreteKeySpec );
        return cipher.doFinal ( message );
    }
    public static PublicKey getPublicKey(String client_name) throws Exception{
        String publicKeyFolder = "pki/public_keys/";
        FileInputStream fis = new FileInputStream(publicKeyFolder + client_name+"PUk.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PublicKey publicKey = (PublicKey) ois.readObject();
        ois.close();
        return publicKey;
    }
    public static byte[] encryptRSA ( byte[] message , Key publicKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.ENCRYPT_MODE , publicKey );
        return cipher.doFinal ( message );
    }

    public static byte[] decryptRSA ( byte[] message , Key privateKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.DECRYPT_MODE , privateKey );
        return cipher.doFinal ( message );
    }
    public static KeyPair generateKeyPair () throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance ( "RSA" );
        keyPairGenerator.initialize ( 2048 );
        return keyPairGenerator.generateKeyPair();
    }
    public static void storeRSAKeys(KeyPair keyPair, String client_name) throws Exception{
        // Write the private key to disk
        String privateKeyFolder = client_name + "/private";
        File privateUserKey = new File(privateKeyFolder);
        privateUserKey.mkdirs();

        // Invés de escrever a chave em String, ela é guardada como um objecto PrivateKey
        File privateKeyFile = new File(privateKeyFolder, "privateKey.key");
        FileOutputStream fos = new FileOutputStream(privateKeyFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(keyPair.getPrivate());
        oos.close();

        // Write the public key
        String publicKeyFolder = "pki/public_keys/";
        File publiKeyFile = new File(publicKeyFolder, client_name+ "PUk.key");
        FileOutputStream pubFOS = new FileOutputStream(publiKeyFile);
        ObjectOutputStream pubOOS = new ObjectOutputStream(pubFOS);
        pubOOS.writeObject(keyPair.getPublic());
        pubOOS.close();
    }
    public static void writeDecryptedFile(String client_name,String nomeFicheiro) throws Exception{
        String privateKeyFolder = client_name + "/files";
        File privateUserKey = new File(privateKeyFolder);
        privateUserKey.mkdirs();


        File arquivo = new File(client_name + "/files/ficheiroFinalhahaha.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo));
        bw.write(nomeFicheiro);
        bw.close();
    }
}
