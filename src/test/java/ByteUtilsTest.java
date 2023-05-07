import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ByteUtilsTest {


    @Test
    @DisplayName("Test the split of a message to a array ")
    void splitByteArray() {
        byte[] text = "1112 is number small.".getBytes();
        int chunkSize = 4;
        ArrayList<byte[]> chunks = ByteUtils.splitByteArray(text, chunkSize);

        assertEquals(6, chunks.size());
        assertArrayEquals("1112".getBytes(), chunks.get(0));
        assertArrayEquals(" is ".getBytes(), chunks.get(1));
        assertArrayEquals("numb".getBytes(), chunks.get(2));
        assertArrayEquals("er s".getBytes(), chunks.get(3));
        assertArrayEquals("mall".getBytes(), chunks.get(4));
    }

    @Test
    @DisplayName("Test if concat Byte Array")
    void concatByteArrays() {
        byte[] test1 = "We are, ".getBytes();
        byte[] test2 = "the champions".getBytes();
        byte[] result = ByteUtils.concatByteArrays(test1,test2);

        assertArrayEquals("We are, the champions".getBytes(), result);
    }

    @DisplayName("Test if the computeXor Works  ")
    @Test
    void computeXOR() {
        byte[] test1 = {0x11, 0x22, 0x12};
        byte[] test2 = {0x22, 0x22, 0x42};
        byte[] result = ByteUtils.computeXOR(test1, test2);

        assertArrayEquals(new byte[]{0x33, 0x00, 0x50}, result);
    }


    @DisplayName("Test the Pading os splitbyteArray")
    @Test
        public void testSplitByteArrayWithPadding() {
            byte[] input = {0x11, 0x21, 0x33};
            int chunkSize = 5;

            ArrayList<byte[]> result = ByteUtils.splitByteArray(input, chunkSize);
            assertEquals(1, result.size());

            byte[]  test= {0x11, 0x21, 0x33,0x02,0x02};
           assertArrayEquals(test, result.get(0));

        }


        @DisplayName("testPadding")
    @Test
    public void testPadding() {

        int Size = 8;
        byte[] text = "istoeumteste".getBytes(); // tamanho 16

        ArrayList<byte[]> chunks = ByteUtils.splitByteArray(text, Size);
        assertEquals(text.length / Size, chunks.size());

        byte[] lastChunk = chunks.get(chunks.size() - 1);
        assertEquals(Size, lastChunk.length);

        byte expectedPadding = (byte) (Size - text.length % Size);
        assertEquals(expectedPadding, lastChunk[Size - 1]);
    }
    }