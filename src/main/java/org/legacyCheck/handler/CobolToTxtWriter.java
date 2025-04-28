package org.legacyCheck.handler;

import org.legacyCheck.reader.CobolFileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CobolToTxtWriter {

    private final CobolFileReader cobolFileReader;

    public CobolToTxtWriter(CobolFileReader cobolFileReader) {
        this.cobolFileReader = cobolFileReader;
    }

    public void createEmptyTxtForCobolFiles(String cobolDirectory, String outputDirectory) {
        try {
            // Obtém os arquivos COBOL usando a função existente
            List<Path> cobolFiles = cobolFileReader.getCobolFiles(cobolDirectory);
            if (cobolFiles.isEmpty()) {
                System.out.println("Nenhum arquivo .cbl ou .cob encontrado no diretório.");
                return;
            }
            // Cria os arquivos .txt correspondentes
            for (Path cobolFile : cobolFiles) {
                String txtFileName = cobolFile.getFileName().toString().replaceFirst("\\.(cbl|cob)$", ".txt");
                if(cobolFile.getFileName().toString().contains("FTB.CBL")){
                    txtFileName = cobolFile.getFileName().toString().replace("FTB.CBL"," - Response_AI.txt");
                } else if (cobolFile.getFileName().toString().contains(".CBL")) {
                    txtFileName = cobolFile.getFileName().toString().replace(".CBL"," - Response_AI.txt");
                } else {
                    txtFileName = cobolFile.getFileName().toString().replaceFirst("\\.(cbl|cob)$", ".txt");

                }
                Path txtFilePath = Path.of(outputDirectory, txtFileName);

                // Verifica se o arquivo já existe antes de criar
                if (Files.exists(txtFilePath)) {
                    System.out.println("Arquivo já existe, ignorando: " + txtFilePath);
                    continue;
                }
                // Cria o arquivo .txt vazio
                Files.createDirectories(txtFilePath.getParent()); // Garante que o diretório de saída exista
                Files.createFile(txtFilePath);
                System.out.println("Arquivo criado: " + txtFilePath);
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivos .txt: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
