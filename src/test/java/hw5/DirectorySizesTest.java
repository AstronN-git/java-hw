package hw5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectorySizesTest {
    @TempDir
    Path directory;

    @Test
    void listsDirectFilesAndSumsTheirByteSizes() throws IOException {
        Files.writeString(directory.resolve("text.txt"), "ёж");
        Files.createFile(directory.resolve("empty.txt"));
        Path nested = Files.createDirectory(directory.resolve("nested"));
        Files.write(nested.resolve("other.bin"), new byte[10]);
        Files.write(directory.resolve("data.bin"), new byte[3]);

        DirectorySizes sizes = DirectorySizes.list(directory);

        assertEquals(List.of(
                new DirectorySizes.FileSize("data.bin", 3),
                new DirectorySizes.FileSize("empty.txt", 0),
                new DirectorySizes.FileSize("text.txt", 4)), sizes.files());
        assertEquals(7, sizes.totalBytes());
    }

    @Test
    void returnsZeroForAnEmptyDirectory() throws IOException {
        assertEquals(new DirectorySizes(List.of(), 0), DirectorySizes.list(directory));
    }
}
