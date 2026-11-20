package hw5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public record DirectorySizes(List<FileSize> files, long totalBytes) {
    public DirectorySizes {
        files = List.copyOf(files);
    }

    public record FileSize(String name, long bytes) {
    }

    public static DirectorySizes list(Path directory) throws IOException {
        List<FileSize> files = new ArrayList<>();
        long totalBytes = 0;
        try (Stream<Path> entries = Files.list(directory)) {
            Iterator<Path> iterator = entries.sorted().iterator();
            while (iterator.hasNext()) {
                Path entry = iterator.next();
                if (Files.isRegularFile(entry)) {
                    long bytes = Files.size(entry);
                    files.add(new FileSize(entry.getFileName().toString(), bytes));
                    totalBytes += bytes;
                }
            }
        }
        return new DirectorySizes(files, totalBytes);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: DirectorySizes <directory>");
            return;
        }
        DirectorySizes sizes = list(Path.of(args[0]));
        for (FileSize file : sizes.files()) {
            System.out.printf("%s\t%d bytes%n", file.name(), file.bytes());
        }
        System.out.printf("Total: %d bytes%n", sizes.totalBytes());
    }
}
