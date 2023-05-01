import javax.crypto.Cipher;
import java.security.*;
import java.io.*;
import java.util.ArrayList;

public class RSA {
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

    public static PrivateKey getPrivateKey(String client_name) throws Exception{
        String privateKeyFolder = client_name + "/private";
        FileInputStream fis = new FileInputStream(privateKeyFolder + "/privateKey.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PrivateKey privateKey = (PrivateKey) ois.readObject();
        ois.close();
        return privateKey;
    }
    public static PublicKey getPublicKey(String client_name) throws Exception{
        String publicKeyFolder = "pki/public_keys/";
        FileInputStream fis = new FileInputStream(publicKeyFolder + "/publicKey.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PublicKey publicKey = (PublicKey) ois.readObject();
        ois.close();
        return publicKey;
    }
    public static byte[] encryptRSA ( byte[] message , PublicKey publicKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.ENCRYPT_MODE , publicKey );
        return cipher.doFinal ( message );
    }

    public static byte[] decryptRSA ( byte[] message , PrivateKey privateKey ) throws Exception {
        Cipher cipher = Cipher.getInstance ( "RSA" );
        cipher.init ( Cipher.DECRYPT_MODE , privateKey );
        return cipher.doFinal ( message );
    }


    public static void writeDecryptedFile(String client_name,String nomeFicheiro) throws Exception{
        String privateKeyFolder = client_name + "/files";
        File privateUserKey = new File(privateKeyFolder);
        privateUserKey.mkdirs();


        File arquivo = new File(client_name + "/files/ficheiroFinal.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo));
        bw.write(nomeFicheiro);
        bw.close();
    }




}