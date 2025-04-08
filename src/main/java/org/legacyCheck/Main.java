package org.legacyCheck;

import org.legacyCheck.config.BaseConfig;
import org.legacyCheck.config.SSLUtils;
import org.legacyCheck.reader.CobolFileReader;
import org.legacyCheck.service.OpenAIService;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Desativa a validação SSL
        SSLUtils.disableSSLValidation();
        // Verifica se a chave da OpenAI está presente
        List<Path> files = new CobolFileReader().getCobolFiles(BaseConfig.Path_TO_COBOL_FILES);
        for (Path file : files) {
            // Lê o conteúdo do arquivo COBOL
            String content = new CobolFileReader().readContent(file);
            System.out.println("Analisando arquivo: " + file.getFileName());
            // Envia o conteúdo para a IA
            String review = OpenAIService.analyzeCode(content);
            System.out.println("Resposta da IA: " + review);
        }
    }
}