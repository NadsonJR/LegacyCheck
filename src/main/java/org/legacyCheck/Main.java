package org.legacyCheck;

import org.legacyCheck.config.BaseConfig;
import org.legacyCheck.handler.CobolToTxtWriter;
import org.legacyCheck.handler.cobolHandler;
import org.legacyCheck.pdf.PDFGeneratorBOX;
import org.legacyCheck.reader.CobolFileReader;
import org.legacyCheck.reader.TxtFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        CobolFileReader reader = new CobolFileReader();
        CobolToTxtWriter writer = new CobolToTxtWriter(reader);
        // Desativa a validação SSL
        //SSLUtils.disableSSLValidation();

        // Lê os arquivos COBOL do diretório especificado
        //List<Path> files = new CobolFileReader().getCobolFiles(BaseConfig.Path_TO_COBOL_FILES);
        //cobolHandler cobolHandler = new cobolHandler();
        //cobolHandler.splitCobolFilesV3(BaseConfig.OutputCobolSplitFiles, files, 4800);

        writer.createEmptyTxtForCobolFiles(BaseConfig.Path_TO_COBOL_FILES, BaseConfig.TxtPath);
        // Lê os arquivos TXT do diretório especificado
        List<Path> txtFiles = new TxtFileReader().getTxtFiles(BaseConfig.TxtPath);

        for (Path txtfile : txtFiles) {
            // Lê o conteúdo do arquivo TXT
            String content = new TxtFileReader().readContent(txtfile);
            //System.out.println("Analisando arquivo: " + txtfile.getFileName());
            PDFGeneratorBOX pdfGeneratorBOX = new PDFGeneratorBOX();
            String PdfFile = txtfile.getFileName().toString().replace(".txt", "");
            pdfGeneratorBOX.generatePDF(content, BaseConfig.PDFPath,PdfFile+ " BOX");

            // Envia o conteúdo para a IA
//            String review = OpenAIService.analyzeCode(content);
//            System.out.println("Resposta da IA: " + review);
        }

        //cobolHandler calculator = new cobolHandler();
        //calculator.calculateTokensForDirectory(BaseConfig.Path_TO_COBOL_FILES);

//        for (Path file : files) {
//            // Lê o conteúdo do arquivo COBOL
//            String content = new CobolFileReader().readContent(file);
//            System.out.println("Analisando arquivo: " + file.getFileName());
//
//            // Envia o conteúdo para a IA
//            String review = OpenAIService.analyzeCode(content);
//            System.out.println("Resposta da IA: " + review);
//        }
    }
}