package hw5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextStatisticsTest {
    @TempDir
    Path directory;

    @Test
    void countsRussianUtf8TextIncludingABlankLine() throws Exception {
        Path file = Path.of(getClass().getResource("russian.txt").toURI());

        assertEquals(new TextStatistics(3, 4, 15), TextStatistics.count(file));
    }

    @Test
    void countsUnicodeLettersAndRecognizesWordBoundaries() throws Exception {
        Path file = Files.writeString(directory.resolve("unicode.txt"), "ёж-икс 42\r\n𐐀б\t🙂A");

        assertEquals(new TextStatistics(2, 4, 8), TextStatistics.count(file));
    }

    @Test
    void returnsZerosForAnEmptyFile() throws Exception {
        Path file = Files.createFile(directory.resolve("empty.txt"));

        assertEquals(new TextStatistics(0, 0, 0), TextStatistics.count(file));
    }
}
