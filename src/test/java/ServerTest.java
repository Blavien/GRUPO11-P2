import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    Server server = new Server ( 8001 );
    Thread serverThread = new Thread ( server );

    ServerTest() throws Exception {
    }






@DisplayName("Get and set the a new server port")
    @Test
    void getSetPort() {
        serverThread.start();
        server.setPort(2020);
        assertEquals(2020,server.getPort());
    }


}