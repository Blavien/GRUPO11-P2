import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class RequestUtilsTest {

    private static String REGISTRY_PATH = "server/Registry/registry.txt";

    @BeforeEach
    public void setUp() throws Exception {
        // cria um novo arquivo de registro vazio antes de cada teste
        RequestUtils.emptyRegistry();
    }

    @Test
    public void testGetAbsoluteFilePath() {
        String request = "test.txt";
        String expectedPath = "server/files/test.txt";
        assertEquals(expectedPath, RequestUtils.getAbsoluteFilePath(request));
    }

    @Test
    public void testGetFileNameFromRequest() {
        String request = "GET : test.txt";
        String expectedName = "test.txt";
        assertEquals(expectedName, RequestUtils.getFileNameFromRequest(request));
    }

    @Test
    public void testSplitRequest() throws Exception {
        String message = "CLIENT1: GET : test.txt";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("CLIENT1");
        expected.add("test.txt");
        assertEquals(expected, RequestUtils.splitRequest(message));
    }

    @Test
    public void tesifFileExist() throws IOException {
        RequestUtils.newClientRegister("CLIENT1");
        REGISTRY_PATH = "src/test/resources/test-write.txt";
        File file = new File(REGISTRY_PATH);
        assertTrue(file.exists());
    }

    @Test
    public void testRegisterRequests() throws IOException {
        RequestUtils.newClientRegister("CLIENT1");
        ArrayList<String> request = new ArrayList<>();
        request.add("CLIENT1");
        request.add("test.txt");
        RequestUtils.registerRequests(request);
        File file = new File(REGISTRY_PATH);
        assertTrue(file.exists());
        assertEquals("CLIENT1 : 1", Files.readAllLines(file.toPath()).get(0));
    }

    @Test
    public void testEmptyRegistry() throws Exception {
        RequestUtils.newClientRegister("CLIENT1");
        RequestUtils.emptyRegistry();
        File file = new File(REGISTRY_PATH);
        assertTrue(file.exists());
        assertEquals(0, Files.readAllLines(file.toPath()).size());
    }



    @Test
    public void testResetRequestCounter() throws IOException {
        RequestUtils.newClientRegister("CLIENT1");
        ArrayList<String> request = new ArrayList<>();
        request.add("CLIENT1");
        request.add("test.txt");
        RequestUtils.registerRequests(request);
        RequestUtils.resetRequestCounter("CLIENT1");
        File file = new File(REGISTRY_PATH);
        assertTrue(file.exists());
        assertEquals("CLIENT1 : 0", Files.readAllLines(file.toPath()).get(0));
    }







}
