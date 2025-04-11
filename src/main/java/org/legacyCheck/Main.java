package org.legacyCheck;

import org.legacyCheck.config.BaseConfig;
import org.legacyCheck.config.SSLUtils;
import org.legacyCheck.pdf.PDFGenerator;
import org.legacyCheck.pdf.PDFGeneratorBOX;
import org.legacyCheck.reader.CobolFileReader;
import org.legacyCheck.reader.TxtFileReader;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Desativa a validação SSL
        //SSLUtils.disableSSLValidation();

        // Lê os arquivos COBOL do diretório especificado
        //List<Path> files = new CobolFileReader().getCobolFiles(BaseConfig.Path_TO_COBOL_FILES);

        // Lê os arquivos TXT do diretório especificado
        List<Path> txtFiles = new TxtFileReader().getTxtFiles(BaseConfig.TxtPath);

        for (Path txtfile : txtFiles) {
            // Lê o conteúdo do arquivo TXT
            String content = new TxtFileReader().readContent(txtfile);
            System.out.println("Analisando arquivo: " + txtfile.getFileName());
            PDFGenerator pdfGenerator = new PDFGenerator();
            PDFGeneratorBOX pdfGeneratorBOX = new PDFGeneratorBOX();
            String PdfFile = txtfile.getFileName().toString().replace(".txt", "");
            //pdfGenerator.generatePDF(content, BaseConfig.TxtPath + PdfFile + ".pdf",PdfFile);
            pdfGeneratorBOX.generatePDF(content, BaseConfig.TxtPath + PdfFile,PdfFile);
            // Envia o conteúdo para a IA
//            String review = OpenAIService.analyzeCode(content);
//            System.out.println("Resposta da IA: " + review);
        }

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