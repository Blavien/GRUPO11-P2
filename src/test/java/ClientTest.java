import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {





    @Test
    void setGetClientName() throws Exception {
        Server server = new Server ( 8200 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("francisco".getBytes());
        System.setIn(in);


        Client client = new Client ( 8200);
        assertEquals("francisco",client.getClientName());


        client.setClientName("joao");
        assertEquals("joao",client.getClientName());
        System.setIn(sysInBackup);
    }


    @Test
    void setGetPrivateKey() throws Exception {
        Server server = new Server ( 8201 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("francisco".getBytes());
        System.setIn(in);


        Client client = new Client ( 8201);
        PrivateKey bomdiaKey = RSA.getPrivateKey(client.getClientName());
        client.setPrivateKey();
        assertEquals(bomdiaKey, client.getPrivateKey());

        System.setIn(sysInBackup);
    }

    @Test
    void setGetPublicKey() throws Exception {
        Server server = new Server ( 8202 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("francisco".getBytes());
        System.setIn(in);


        Client client = new Client ( 8202);
        PublicKey bomdiaKey = RSA.getPublicKey(client.getClientName());
        client.setPublicKey();
        assertEquals(bomdiaKey, client.getPublicKey());

        System.setIn(sysInBackup);
    }



    @Test
    void setGetFileName() throws Exception {
        Server server = new Server ( 8205 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("francisco".getBytes());
        System.setIn(in);

        Client client = new Client ( 8205);
        client.setFileName("ola");
        assertEquals("ola", client.getFileName());

        System.setIn(sysInBackup);

    }

    //pgtar se posso remover o setConnected
    @Test
    void setConnected() {
    }


    //nao faço
    @Test
    void doHandshake() throws Exception {
        Server server = new Server ( 8230 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in1 = new ByteArrayInputStream("francisco\n0\n0".getBytes());
        System.setIn(in1);



        Client client = new Client ( 8230);
        assertEquals("francisco", client.getClientName());
        RequestUtils.writeNumberToFile(1,RequestUtils.HANDSHAKE_SIGNAL);
        assertEquals(false,client.doHandshake());

        System.setIn(sysInBackup);


    }





    @Test
    void saveFiles() throws Exception {
        Server server = new Server ( 8400 );
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("francisco".getBytes());
        System.setIn(in);

        Client client = new Client ( 8400);
        client.setClientName("joao");
        client.setFileName("boatarde.txt");
        client.saveFiles("aqui");
        Path pasta = Paths.get(client.getClientName() + "/files");
        //testa se os diretórios foram criados
        assertTrue(Files.exists(pasta));
        assertTrue (Files.isDirectory(pasta));

        Path path = Paths.get("joao" + "/files/User_" + client.getFileName());
        byte[] bytesArquivo = Files.readAllBytes(path);
        String conteudoArquivo = new String(bytesArquivo, StandardCharsets.UTF_8);
        assertEquals("aqui",conteudoArquivo);


        System.setIn(sysInBackup);

    }

    @Test
    void sendRequestAndWritingFile() throws Exception {
        Server server = new Server ( 8700 );
        Thread serverThread = new Thread ( server );
        InputStream sysInBackup = System.in;

        serverThread.start ( );
        String initialString = "francisco\n0\n0";
        InputStream targetStream = IOUtils.toInputStream(initialString);
        System.setIn(targetStream);
        Client client = new Client ( 8700);

        RequestUtils.writeNumberToFile(1,RequestUtils.HANDSHAKE_SIGNAL);

        client.doHandshake();

        client.execute("ola.txt");
        String textoOriginal = new String(Files.readAllBytes(Paths.get("server/files/ola.txt")));
        String texto = new String(Files.readAllBytes(Paths.get("francisco/files/User_ola.txt")));


        assertEquals(textoOriginal,texto);
        System.setIn(sysInBackup);
    }


}