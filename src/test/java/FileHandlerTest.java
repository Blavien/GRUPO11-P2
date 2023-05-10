import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileHandlerTest {
    @DisplayName("Tests if the file is being created and the content aswell")
@Test
    void testWriteAndReadFile() throws IOException {
        String filePath = "src/test/resources/test.txt";
        String expectedContent = "This is a test file.";
        byte[] contentBytes = expectedContent.getBytes();
        FileHandler.writeFile(filePath, contentBytes);
        byte[] fileBytes = FileHandler.readFile(filePath);
        String actualContent = new String(fileBytes);
        Assertions.assertEquals(expectedContent, actualContent);
        File file = new File(filePath);
        file.delete();
    }

}
