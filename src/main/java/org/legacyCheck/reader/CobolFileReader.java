package org.legacyCheck.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CobolFileReader {

    // Method to read COBOL files from a given directory
    public List<Path> getCobolFiles(String directoryPath) {
        List<Path> cobolFiles = new ArrayList<>();
        try{
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".cbl") || name.endsWith(".cob");
                    }).forEach(cobolFiles::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cobolFiles;
    }

    // Method to read the content of a COBOL file
    public String readContent(Path filePath){
        try{
            return Files.readString(filePath);
        } catch (Exception e) {
            System.out.println("Error reading file: " + filePath + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
