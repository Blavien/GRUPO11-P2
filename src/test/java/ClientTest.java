import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    Server server = new Server ( 8000 );
    Thread serverThread = new Thread ( server );


    ClientTest() throws Exception {
    }

    @Test
    void setGetClientName() throws Exception {
        serverThread.start ( );
        Client client = new Client ( 8000,true );
        client.setClientName("joao");
        assertEquals("joao",client.getClientName());
    }


    @Test
    void setGetPrivateKey() throws Exception {
        serverThread.start ( );
        Client client = new Client ( 8000,true );
        PrivateKey bomdiaKey = RSA.getPrivateKey(client.getClientName());
        client.setPrivateKey();
        assertEquals(bomdiaKey, client.getPrivateKey());
    }

    @Test
    void setGetPublicKey() throws Exception {
        serverThread.start ( );
        Client client = new Client ( 8000,true );
        PublicKey bomdiaKey = RSA.getPublicKey(client.getClientName());
        client.setPublicKey();
        assertEquals(bomdiaKey, client.getPublicKey());

    }



    @Test
    void setGetFileName() throws Exception {
        serverThread.start ( );
        Client client = new Client ( 8000,true );
        client.setFileName("ola");
        assertEquals("ola", client.getFileName());

    }

//pgtar se posso remover o setConnected
    @Test
    void setConnected() {
    }


    //nao faço
    @Test
    void doHandshake() {
    }

    @Test
    void sendClientChoice() {
    }

    @Test
    void endConnection() {
    }

    @Test
    void saveFiles() throws Exception {

        serverThread.start ( );
        Client client = new Client ( 8000,true );
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

    }

    @Test
    void sendMessage() throws Exception {
        serverThread.start ( );
        Client client = new Client ( 8000,false );


        client.sendMessage("hahaha");

        assertEquals("hahaha",ClientHandler.getMessageTest());
    }

    @Test
    void execute() {
    }
}