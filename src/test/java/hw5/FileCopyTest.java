package hw5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileCopyTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void copiesBinaryDataAndReplacesExistingContent(boolean buffered) throws IOException {
        byte[] data = new byte[20_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        Path source = Files.write(directory.resolve("source.bin"), data);
        Path target = Files.write(directory.resolve("target.bin"), new byte[data.length + 10]);

        FileCopy.copy(source, target, buffered);

        assertArrayEquals(data, Files.readAllBytes(target));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void copiesAnEmptyFile(boolean buffered) throws IOException {
        Path source = Files.createFile(directory.resolve("empty.bin"));
        Path target = directory.resolve("copy.bin");

        FileCopy.copy(source, target, buffered);

        assertEquals(0, Files.size(target));
    }

    @Test
    void rejectsCopyingAFileOntoItself() throws IOException {
        Path source = Files.writeString(directory.resolve("source.txt"), "данные");

        assertThrows(IOException.class, () -> FileCopy.copy(source, source, true));
        assertEquals("данные", Files.readString(source));
    }
}
