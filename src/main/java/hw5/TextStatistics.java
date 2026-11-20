package hw5;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record TextStatistics(int lines, long words, long letters) {
    public static TextStatistics count(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        long words = 0;
        long letters = 0;
        for (String line : lines) {
            // Слово — непрерывная последовательность букв.
            boolean inWord = false;
            for (int offset = 0; offset < line.length(); ) {
                int codePoint = line.codePointAt(offset);
                boolean letter = Character.isLetter(codePoint);
                if (letter) {
                    letters++;
                    if (!inWord) {
                        words++;
                    }
                }
                inWord = letter;
                offset += Character.charCount(codePoint);
            }
        }
        return new TextStatistics(lines.size(), words, letters);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: TextStatistics <file>");
            return;
        }
        TextStatistics statistics = count(Path.of(args[0]));
        System.out.printf("Lines: %d, words: %d, letters: %d%n",
                statistics.lines(), statistics.words(), statistics.letters());
    }
}
