import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private Message message;
    private byte[] messageBytes;
    private byte[] signatureBytes;

    @BeforeEach
    public void setUp() {
        message = new Message("Bom dia".getBytes());
        messageBytes = "Test message".getBytes();
        signatureBytes = "Test signature".getBytes();
    }

    @Test
    @DisplayName("Test the getMessage")
    void testGetMessage() {
        byte[] expected = "Bom dia".getBytes();
        byte[] actual = message.getMessage();
        assertArrayEquals(expected, actual);
    }

    @Test
    @DisplayName("Test getSignature")
    void testGetSignature() {
        assertNull(message.getSignature());
    }

    @Test
    @DisplayName("Test constructor with signature")
    void testConstructorWithSignature() {
        Message message = new Message(messageBytes, signatureBytes);
        assertArrayEquals(messageBytes, message.getMessage());
        assertArrayEquals(signatureBytes, message.getSignature());
    }

}
