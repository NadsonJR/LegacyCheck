package org.legacyCheck.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFGeneratorBOX {

    private static final float MARGIN = 50;
    private static final float Y_START = 800;
    private float yPosition = Y_START;
    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private PDDocument document;
    private PDFont basicFont;
    private PDFont boldFont;
    private PDFont codeFont;
    private float fontSize = 11;
    private float leading = 15;

    // Method to generate a PDF file from the given content
    public void generatePDF(String content, String outputPath, String fileName) {
        // Verificar se o conteúdo está vazio
        if (content == null || content.trim().isEmpty()) {
            System.out.println("Conteúdo vazio. PDF não será gerado.");
            return;
        }
        try {
            // Define directory for output
            File directory = new File(outputPath);

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //Adjust the file path to include the output folder
            String outputFilePath = outputPath + "/" + fileName + ".pdf";

            // Initialize document and font
            document = new PDDocument();
            basicFont = PDType1Font.HELVETICA;
            boldFont = PDType1Font.HELVETICA_BOLD;
            codeFont = PDType1Font.COURIER;

            // Create first page
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);

            boolean inCobolBlock = false;
            StringBuilder cobolCode = new StringBuilder();
            String currentAuthor = "";
            boolean inTable = false;
            List<String[]> tableData = new ArrayList<>();
            float[] tableWidths = {2, 5, 5};

            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.strip();
                line = line.replace("GitHub Copilot: ", "");

                // Detect the start of a COBOL block
                if (line.equalsIgnoreCase("```cobol")) {
                    inCobolBlock = true;
                    cobolCode = new StringBuilder();
                    continue;
                }

                // Detect the start of a table
                if (line.startsWith("|") && !inTable) {
                    inTable = true;
                    tableData = new ArrayList<>();
                    continue;
                }

                // Detect the end of a table
                if (inTable && (line.isEmpty() || !line.startsWith("|"))) {
                    inTable = false;
                    drawTable(tableData, tableWidths);
                    tableData = null;
                }

                // Process table rows
                if (inTable) {
                    String[] cells = line.split("\\|");
                    List<String> cellContents = new ArrayList<>();
                    for (String cell : cells) {
                        if (!cell.isBlank()) {
                            cellContents.add(cell.strip());
                        }
                    }
                    if (!cellContents.isEmpty()) {
                        tableData.add(cellContents.toArray(new String[0]));
                    }
                    continue;
                }
                // Detect the end of a COBOL block
                if (line.equals("```") && inCobolBlock) {
                    inCobolBlock = false;
                    drawCodeBlock(cobolCode.toString());
                    continue;
                }
                if (inCobolBlock) {
                    cobolCode.append(line).append("\n");
                    continue;
                }
                if (line.equals("---")) {
                    continue;
                }

                if(line.contains("*")){
                    line = line.replace("*", "");
                }
                // Titles and content
                if (line.matches("^#{3,}\\s+.*")) {
                    // ### or more — small title
                    drawTitle(line.replaceFirst("^#{3,}\\s*", ""), 12, new Color(0, 128, 255));
                } else if (line.matches("^##\\s+.*")) {
                    // ## — subtitle medium
                    drawTitle(line.replaceFirst("^##\\s*", ""), 14, new Color(0, 80, 180));
                } else if (line.matches("^#\\s+.*")) {
                    // # — main title
                    drawTitle(line.replaceFirst("^#\\s*", ""), 16, new Color(0, 50, 130));
                } else if (line.isEmpty()) {
                    movePosition(leading);
                } else {
                    drawParagraphWithHighlights(line);
                }
            }
            // Close content stream and save document
            contentStream.close();
            document.save(outputFilePath);
            document.close();
        } catch (Exception e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void movePosition(float distance) {
        yPosition -= distance;
        if (yPosition < MARGIN) {
            try {
                contentStream.close();
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                yPosition = Y_START;
            } catch (IOException e) {
                System.out.println("Error creating new page: " + e.getMessage());
            }
        }
    }

    private void drawTitle(String text, float size, Color color) throws IOException {
        movePosition(size * 1.2f); // margin top
        text = text.replace("**", "");
        contentStream.beginText();
        contentStream.setFont(boldFont, size);
        contentStream.setNonStrokingColor(color);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        movePosition(10); // margin bottom
    }

    private void drawParagraphWithHighlights(String line) throws IOException {
        Pattern pattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = pattern.matcher(line);
        float lineWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        // Split text into normal and highlighted parts
        List<TextPart> parts = new ArrayList<>();
        int lastEnd = 0;
        while (matcher.find()) {
            String before = line.substring(lastEnd, matcher.start());
            if (!before.isEmpty()) {
                parts.add(new TextPart(before, false));
            }
            String highlighted = " " + matcher.group(1);
            parts.add(new TextPart(highlighted, true));
            lastEnd = matcher.end();
        }
        if (lastEnd < line.length()) {
            parts.add(new TextPart(line.substring(lastEnd), false));
        }

        // Detect and color the word "Erro"
        for (int i = 0; i < parts.size(); i++) {
            TextPart part = parts.get(i);
            if (part.text.contains("Erro:")) {
                String[] split = part.text.split("Erro:", -1);
                List<TextPart> newParts = new ArrayList<>();
                for (int j = 0; j < split.length; j++) {
                    if (!split[j].isEmpty()) {
                        newParts.add(new TextPart(split[j], false));
                    }
                    if (j < split.length - 1) {
                        newParts.add(new TextPart("Erro:", true)); // Highlight "Erro"
                    }
                }
                parts.remove(i);
                parts.addAll(i, newParts);
                break;
            }
            if (part.text.contains("Problema:")) {
                String[] split = part.text.split("Problema:", -1);
                List<TextPart> newParts = new ArrayList<>();
                for (int j = 0; j < split.length; j++) {
                    if (!split[j].isEmpty()) {
                        newParts.add(new TextPart(split[j], false));
                    }
                    if (j < split.length - 1) {
                        newParts.add(new TextPart("Problema:", true)); // Highlight "Erro"
                    }
                }
                parts.remove(i);
                parts.addAll(i, newParts);
                break;
            }
        }

        // Detect and color the word "Solução"
        for (int i = 0; i < parts.size(); i++) {
            TextPart part = parts.get(i);
            if (part.text.contains("Solução:")) {
                String[] split = part.text.split("Solução:", -1);
                List<TextPart> newParts = new ArrayList<>();
                for (int j = 0; j < split.length; j++) {
                    if (!split[j].isEmpty()) {
                        newParts.add(new TextPart(split[j], false));
                    }
                    if (j < split.length - 1) {
                        newParts.add(new TextPart("Solução:", true)); // Highlight "Solução"
                    }
                }
                parts.remove(i);
                parts.addAll(i, newParts);
                break;
            }
        }

        // Calculate line breaks
        List<List<TextPart>> lines = new ArrayList<>();
        List<TextPart> currentLine = new ArrayList<>();
        float currentWidth = 0;
        for (TextPart part : parts) {
            String[] words = part.text.split(" ");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (i > 0) {
                    word = " " + word;
                }
                float wordWidth = getStringWidth(word, part.isHighlighted ? codeFont : basicFont, fontSize);
                if (currentWidth + wordWidth > lineWidth) {
                    lines.add(currentLine);
                    currentLine = new ArrayList<>();
                    currentWidth = 0;
                    if (i > 0) {
                        word = word.substring(1); // Remove leading space
                        wordWidth = getStringWidth(word, part.isHighlighted ? codeFont : basicFont, fontSize);
                    }
                }
                currentLine.add(new TextPart(word, part.isHighlighted));
                currentWidth += wordWidth;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        // Draw the lines
        for (List<TextPart> textLine : lines) {
            movePosition(leading);
            float xOffset = MARGIN;
            for (TextPart part : textLine) {
                contentStream.beginText();
                if(part.text.equals("Solução:")){
                    contentStream.setFont(part.isHighlighted ? boldFont : basicFont, fontSize);
                    contentStream.setNonStrokingColor(part.isHighlighted ? new Color(0, 150, 0) : Color.BLACK);
                } else if (part.text.equals("Erro:") || part.text.equals("Problema:")) {
                    contentStream.setFont(part.isHighlighted ? boldFont : basicFont, fontSize);
                    contentStream.setNonStrokingColor(part.isHighlighted ? new Color(255, 0, 0) : Color.BLACK);
                } else {
                    contentStream.setFont(part.isHighlighted ? codeFont : basicFont, fontSize);
                    contentStream.setNonStrokingColor(part.isHighlighted ? new Color(0, 0, 150) : Color.BLACK);
                }
                contentStream.newLineAtOffset(xOffset, yPosition);
                contentStream.showText(part.text);
                contentStream.endText();
                xOffset += getStringWidth(part.text, part.isHighlighted ? codeFont : basicFont, fontSize);
            }
        }
    }

    private void drawCodeBlock(String code) throws IOException {
        movePosition(5); // margin top
        // Draw background
        float codeWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        String[] lines = code.split("\n");
        float codeHeight = lines.length * 12 + 10; // height + padding
        // Check if we need a new page
        if (yPosition - codeHeight < MARGIN) {
            contentStream.close();
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            yPosition = Y_START;
        }
        // Draw background
        contentStream.setNonStrokingColor(new Color(245, 245, 245));
        contentStream.addRect(MARGIN, yPosition - codeHeight, codeWidth, codeHeight);
        contentStream.fill();
        // Draw border
        contentStream.setStrokingColor(new Color(220, 220, 220));
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(MARGIN, yPosition - codeHeight, codeWidth, codeHeight);
        contentStream.stroke();
        // Draw text
        float y = yPosition - 10; // Initial position with padding
        float fontSize = 9;
        for (String line : lines) {
            float x = MARGIN + 5; // Initial position with padding
            String[] words = line.split("(?<=\\s)|(?=\\s)", -1); // Mantém espaços
            for (String word : words) {
                Color textColor = Color.BLACK;
                PDFont font = codeFont;
                if (word.matches("(?i)\\b(MOVE|IF|ELSE|END-IF|PERFORM|EXEC|SQL|END-EXEC|OPEN|CLOSE|READ|WRITE|DISPLAY|STOP RUN|CALL|EVALUATE|WHEN|END-EVALUATE)\\b")) {
                    textColor = new Color(0, 0, 200); // azul
                } else if (word.trim().startsWith("*")) {
                    textColor = new Color(0, 128, 0); // verde
                } else if (word.matches("'[^']*'|\"[^\"]*\"")) {
                    textColor = new Color(153, 102, 0); // marrom
                } else if (word.matches("\\d+")) {
                    textColor = new Color(102, 0, 153); // roxo
                }
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.setNonStrokingColor(textColor);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(word);
                contentStream.endText();
                x += getStringWidth(word, font, fontSize);
            }
            y -= 12; // Line height for code
        }
        yPosition -= codeHeight + 5; // Move down + margin bottom
    }

    private void drawTable(List<String[]> tableData, float[] columnWidths) throws IOException {
        if (tableData == null || tableData.isEmpty()) return;

        float tableWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        float y = yPosition;

        // Calculate the actual column widths
        float[] actualColumnWidths = new float[columnWidths.length];
        float totalWidthRatio = 0;
        for (float width : columnWidths) {
            totalWidthRatio += width;
        }
        for (int i = 0; i < columnWidths.length; i++) {
            actualColumnWidths[i] = (columnWidths[i] / totalWidthRatio) * tableWidth;
        }

        for (String[] row : tableData) {
            float maxRowHeight = 0; // Maximum row height
            List<List<String>> wrappedText = new ArrayList<>();

            // Wrap text for each cell and calculate row height
            for (int i = 0; i < actualColumnWidths.length && i < row.length; i++) {
                float cellWidth = actualColumnWidths[i] - 10; // Account for padding
                List<String> lines = wrapText(row[i], cellWidth, basicFont, fontSize);
                wrappedText.add(lines);
                maxRowHeight = Math.max(maxRowHeight, lines.size() * leading);
            }

            // Check if a new page is needed
            if (y - maxRowHeight < MARGIN) {
                contentStream.close();
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                y = Y_START;
            }

            // Draw cell backgrounds and borders
            float x = MARGIN;
            for (int i = 0; i < actualColumnWidths.length && i < row.length; i++) {
                contentStream.setNonStrokingColor(new Color(240, 240, 240)); // Background color
                contentStream.addRect(x, y - maxRowHeight, actualColumnWidths[i], maxRowHeight);
                contentStream.fill();
                contentStream.setStrokingColor(Color.LIGHT_GRAY); // Border color
                contentStream.setLineWidth(0.5f);
                contentStream.addRect(x, y - maxRowHeight, actualColumnWidths[i], maxRowHeight);
                contentStream.stroke();
                x += actualColumnWidths[i];
            }

            // Draw cell text
            x = MARGIN;
            for (int i = 0; i < actualColumnWidths.length && i < row.length; i++) {
                List<String> lines = wrappedText.get(i);
                float textHeight = lines.size() * leading; // Total text height
                float textY = 5 + y - (maxRowHeight - textHeight) / 2 - leading; // Center text vertically

                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.setFont(basicFont, fontSize);
                    contentStream.setNonStrokingColor(Color.BLACK);
                    contentStream.newLineAtOffset(x + 5, textY); // Add horizontal padding
                    contentStream.showText(line);
                    contentStream.endText();
                    textY -= leading; // Move to the next line
                }
                x += actualColumnWidths[i];
            }
            y -= maxRowHeight; // Move to the next row
        }
        yPosition = y; // Update vertical position
    }

    private List<String> wrapText(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
            if (textWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.length() == 0 ? word : " " + word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }


    private float getStringWidth(String text, PDFont font, float fontSize) throws IOException {
        return font.getStringWidth(text) / 1000 * fontSize;
    }

    private static class TextPart {
        String text;
        boolean isHighlighted;

        TextPart(String text, boolean isHighlighted) {
            this.text = text;
            this.isHighlighted = isHighlighted;
        }
    }
}
