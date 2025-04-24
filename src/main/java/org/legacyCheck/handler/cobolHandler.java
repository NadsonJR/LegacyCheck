package org.legacyCheck.handler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class cobolHandler {

    public void splitCobolFiles(String outputFilePath, List<Path> cobolFiles, int linesPerFile) throws IOException {
        for (Path cobolFile : cobolFiles) {
            File inputFile = cobolFile.toFile();
            String baseFileName = inputFile.getName().replace(".CBL", "").replace(".cbl", "");
            try (BufferedReader reader = Files.newBufferedReader(cobolFile, java.nio.charset.StandardCharsets.ISO_8859_1)) {
                int lineCount = 0;
                int fileCount = 1;
                BufferedWriter writer = null;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 6 && line.charAt(6) == '*') {
                        continue;
                    }
                    // Verifica se a linha contém "Eccox"
                    if (!line.contains("Eccox")) {
                        continue;
                    }
                    if (lineCount % linesPerFile == 0) {
                        if (writer != null) {
                            writer.close();
                        }
                        String outputFileName = outputFilePath + File.separator + baseFileName + "_" + fileCount + ".cbl";
                        writer = Files.newBufferedWriter(Paths.get(outputFileName));
                        fileCount++;
                    }
                    writer.write(line);
                    writer.newLine();
                    lineCount++;
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    // criar criteiros para filtros de section e dentro do filtro verificar o eccox
    public void splitCobolFilesV3(String outputFilePath, List<Path> cobolFiles, int linesPerFile) throws IOException {
        for (Path cobolFile : cobolFiles) {
            File inputFile = cobolFile.toFile();
            String baseFileName = inputFile.getName().replace(".CBL", "").replace(".cbl", "");
            try (BufferedReader reader = Files.newBufferedReader(cobolFile, java.nio.charset.StandardCharsets.ISO_8859_1)) {
                int fileCount = 1;
                int currentLineCount = 0;
                BufferedWriter writer = null;
                StringBuilder currentBlock = new StringBuilder();
                String line;
                boolean insideSection = false;

                while ((line = reader.readLine()) != null) {
                    // Ignorar comentários
                    if (line.length() > 6 && line.charAt(6) == '*') {
                        continue;
                    }

                    // Verificar início de uma SECTION
                    if (line.trim().endsWith("SECTION.")) {
                        insideSection = true;
                    }

                    // Adicionar linha ao bloco atual
                    currentBlock.append(line).append(System.lineSeparator());
                    currentLineCount++;

                    // Verificar fim de uma lógica com EXIT
                    if (line.trim().equals("EXIT.")) {
                        insideSection = false;
                    }

                    // Se o limite de linhas for atingido e não estamos no meio de uma SECTION
                    if (currentLineCount >= linesPerFile && !insideSection) {
                        if (writer != null) {
                            writer.close();
                        }
                        String outputFileName = outputFilePath + File.separator + baseFileName + "_" + fileCount + ".cbl";
                        writer = Files.newBufferedWriter(Paths.get(outputFileName));
                        writer.write(currentBlock.toString());
                        writer.close();
                        fileCount++;
                        currentLineCount = 0;
                        currentBlock.setLength(0); // Limpar o bloco atual
                    }
                }

                // Escrever o restante do bloco no último arquivo
                if (currentBlock.length() > 0) {
                    if (writer != null) {
                        writer.close();
                    }
                    String outputFileName = outputFilePath + File.separator + baseFileName + "_" + fileCount + ".cbl";
                    writer = Files.newBufferedWriter(Paths.get(outputFileName));
                    writer.write(currentBlock.toString());
                    writer.close();
                }
            }
        }
    }

    public void calculateTokensForDirectory(String directoryPath) throws IOException {
        int mediaTotal = 0;
        int contatorArquivo = 0;
        int arquivoDona = 0;
        try (Stream<Path> files = Files.list(Paths.get(directoryPath))) {
            for (Path cobolFile : files.filter(file -> file.toString().endsWith(".cbl") || file.toString().endsWith(".CBL"))
                    .collect(Collectors.toList())) {
                int tokens = calculateTokensForFile(cobolFile);
                if(tokens<120000){
                    arquivoDona++;
                }
                contatorArquivo++;
                mediaTotal = mediaTotal + tokens;
                System.out.println("Arquivo: " + cobolFile.getFileName() + " - Tokens estimados: " + tokens);
            }
            System.out.println("Arquivos com menos de 120000 tokens: " + arquivoDona);
            mediaTotal = mediaTotal / contatorArquivo;
            System.out.println("Média de tokens usados: " + mediaTotal);
        }
    }

    private int calculateTokensForFile(Path cobolFilePath) throws IOException {
        StringBuilder content = new StringBuilder();
        // Lê o conteúdo do arquivo COBOL
        try (BufferedReader reader = Files.newBufferedReader(cobolFilePath, java.nio.charset.StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignorar comentários no COBOL
//                if (line.length() > 6 && line.charAt(6) == '*') {
//                    continue;
//                }
                content.append(line).append(System.lineSeparator());
            }
        }
        // Calcula o número de caracteres
        int characterCount = content.length();
        // Calcula o número de tokens (estimativa: 4 caracteres por token)
        return characterCount / 4;
    }

}
