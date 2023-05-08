import org.junit.Test;
import static org.junit.Assert.*;

public class MainServerTest {

    private Server server = new Server(8000);

    public MainServerTest() throws Exception {
    }

    @Test
    public void testServerThreadStarts() throws Exception {
        RequestUtils.emptyRegistry();
        Thread serverThread = new Thread ( server );
        serverThread.start ( );

        assertTrue(serverThread.isAlive());
       server.setPort(8000);
       assertEquals(server.getPort(),8000);
    }


}
