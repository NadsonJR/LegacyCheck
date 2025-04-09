package org.legacyCheck.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.BorderRadius;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PDFGenerator {

    // Method to generate a PDF file from the given content

    public void generatePDF(String content, String outputPath) {
        try {
            PdfWriter writer = new PdfWriter(outputPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            document.setFont(font);
            document.setFontSize(11);

            boolean inCobolBlock = false;
            StringBuilder cobolCode = new StringBuilder();
            String currentAuthor = "";

            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.strip();
                line = line.replace("NadsonJR", "Antonio Gaido");

                // Detecta início de bloco cobol
                if (line.equals("```cobol")) {
                    inCobolBlock = true;
                    cobolCode = new StringBuilder();
                    continue;
                }

                // Detecta fim de bloco cobol
                if (line.equals("```") && inCobolBlock) {
                    inCobolBlock = false;
                    document.add(formatCobolCodeBlock(cobolCode.toString()));
                    continue;
                }
                if (inCobolBlock) {
                    cobolCode.append(line).append("\n");
                    continue;
                }

                if (line.equals("---")) {
                   line.replace("---", "");
                    continue;
                }

                // Autores
                if (line.endsWith(":")) {
                    currentAuthor = line.replace(":", "").trim();
                    Color bgColor = currentAuthor.equalsIgnoreCase("GitHub Copilot")
                            ? ColorConstants.LIGHT_GRAY
                            : new DeviceRgb(220, 240, 255);

                    Paragraph authorHeader = new Paragraph(currentAuthor)
                            .setFontSize(12)
                            .setBackgroundColor(bgColor)
                            .setPadding(5)
                            .setMarginTop(10)
                            .setMarginBottom(5)
                            .setNeutralRole().simulateBold();
                    document.add(authorHeader);
                    continue;
                }

                // Títulos e conteúdo
                if (line.matches("^#{3,}\\s+.*")) {
                    // ### ou mais — título pequeno
                    document.add(new Paragraph(line.replaceFirst("^#{3,}\\s*", ""))
                            .setFontSize(13)
                            .setFontColor(new DeviceRgb(0, 128, 255))
                            .setMarginTop(10)
                            .setNeutralRole().simulateBold());
                } else if (line.matches("^##\\s+.*")) {
                    // ## — subtítulo médio
                    document.add(new Paragraph(line.replaceFirst("^##\\s*", ""))
                            .setFontSize(15)
                            .setFontColor(new DeviceRgb(0, 80, 180))
                            .setMarginTop(15)
                            .setNeutralRole().simulateBold());
                } else if (line.matches("^#\\s+.*")) {
                    // # — título principal
                    document.add(new Paragraph(line.replaceFirst("^#\\s*", ""))
                            .setFontSize(18)
                            .setFontColor(new DeviceRgb(0, 50, 130))
                            .setMarginTop(20)
                            .setMarginBottom(10)
                            .setNeutralRole().simulateBold());
                }  else if (line.isEmpty()) {
                    document.add(new Paragraph(" "));
                } else {
                    document.add(formatParagraphWithHighlights(line));
                }
            }

            document.close();
        } catch (Exception e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    // Realça palavras entre crase `...` com cor verde
    private Paragraph formatParagraphWithHighlights(String line) {
        Paragraph paragraph = new Paragraph();
        Pattern pattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = pattern.matcher(line);

        int lastEnd = 0;
        while (matcher.find()) {
            String before = line.substring(lastEnd, matcher.start());
            String highlighted = matcher.group(1);

            paragraph.add(new Text(before));
            paragraph.add(new Text(highlighted)
                    .setFontColor(new DeviceRgb(0, 150, 0))
                    .setNeutralRole().simulateBold());
            lastEnd = matcher.end();
        }

        if (lastEnd < line.length()) {
            paragraph.add(new Text(line.substring(lastEnd)));
        }

        return paragraph;
    }

    // Formata bloco cobol com sintaxe colorida
    private Paragraph formatCobolCodeBlock(String code) throws IOException {
        Paragraph paragraph = new Paragraph()
                .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                .setFontSize(9)
                .setBackgroundColor(new DeviceRgb(245, 245, 245))
                .setPadding(5)
                .setMarginTop(5)
                .setMarginBottom(5)
                .setBorderRadius(new BorderRadius(5))
                .setMultipliedLeading(1.0f); // Mantém altura de linha fixa

        String[] lines = code.split("\n");
        for (String line : lines) {
            String[] words = line.split("(?<=\\s)|(?=\\s)", -1); // Mantém espaços

            for (String word : words) {
                Text text = new Text(word);

                if (word.matches("(?i)\\b(MOVE|IF|ELSE|END-IF|PERFORM|EXEC|SQL|END-EXEC|OPEN|CLOSE|READ|WRITE|DISPLAY|STOP RUN|CALL|EVALUATE|WHEN|END-EVALUATE)\\b")) {
                    text.setFontColor(new DeviceRgb(0, 0, 200)) // azul
                            .setNeutralRole().simulateBold();
                } else if (word.trim().startsWith("*")) {
                    text.setFontColor(new DeviceRgb(0, 128, 0)); // verde
                } else if (word.matches("'[^']*'|\"[^\"]*\"")) {
                    text.setFontColor(new DeviceRgb(153, 102, 0)); // marrom
                } else if (word.matches("\\d+")) {
                    text.setFontColor(new DeviceRgb(102, 0, 153)); // roxo
                }

                paragraph.add(text);
            }
            paragraph.add("\n");
        }
        return paragraph;
    }

}
