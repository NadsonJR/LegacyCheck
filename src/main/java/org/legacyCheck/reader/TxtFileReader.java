package org.legacyCheck.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TxtFileReader {

    public List<Path> getTxtFiles(String directoryPath) {
        List<Path> txtFiles = new ArrayList<>();
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase().endsWith(".txt"))
                    .forEach(txtFiles::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return txtFiles;
    }

    // Method to read the content of a text file
    public String readContent(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (Exception e) {
            System.out.println("Error reading file: " + filePath + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
