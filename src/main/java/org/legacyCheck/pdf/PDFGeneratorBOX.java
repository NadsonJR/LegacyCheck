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
    private static final float Y_START = 750;
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
        try {
            // Define directory for output
            String outputDirectory = "src/main/resources/ReponsePrompts/PDFs";
            File directory = new File(outputDirectory);

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //Adjust the file path to include the output folder
            String outputFilePath = outputDirectory + "/" + fileName + ".pdf";

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
                line = line.replace("NadsonJR", "Antonio Gaido");

                // Detect the start of a COBOL block
                if (line.equals("```cobol")) {
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
                // Autores
                if (line.endsWith(":")) {
                    currentAuthor = line.replace(":", "").trim();
                    Color bgColor = currentAuthor.equalsIgnoreCase("GitHub Copilot")
                            ? Color.LIGHT_GRAY
                            : new Color(220, 240, 255);

                    drawAuthorHeader(currentAuthor, bgColor);
                    continue;
                }
                // Titles and content
                if (line.matches("^#{3,}\\s+.*")) {
                    // ### or more — small title
                    drawTitle(line.replaceFirst("^#{3,}\\s*", ""), 13, new Color(0, 128, 255));
                } else if (line.matches("^##\\s+.*")) {
                    // ## — subtitle medium
                    drawTitle(line.replaceFirst("^##\\s*", ""), 15, new Color(0, 80, 180));
                } else if (line.matches("^#\\s+.*")) {
                    // # — main title
                    drawTitle(line.replaceFirst("^#\\s*", ""), 18, new Color(0, 50, 130));
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

    private void drawAuthorHeader(String author, Color bgColor) throws IOException {
        float height = 25;
        movePosition(10); // margin top
        // Draw background
        contentStream.setNonStrokingColor(bgColor);
        contentStream.addRect(MARGIN, yPosition - height, PDRectangle.A4.getWidth() - 2 * MARGIN, height);
        contentStream.fill();
        // Draw text
        contentStream.beginText();
        contentStream.setFont(boldFont, 12);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(MARGIN + 5, yPosition - 15);
        contentStream.showText(author);
        contentStream.endText();
        movePosition(height + 5); // + margin bottom
    }

    private void drawTitle(String text, float size, Color color) throws IOException {
        movePosition(size * 1.2f); // margin top
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
            String highlighted = matcher.group(1);
            parts.add(new TextPart(highlighted, true));
            lastEnd = matcher.end();
        }
        if (lastEnd < line.length()) {
            parts.add(new TextPart(line.substring(lastEnd), false));
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
                contentStream.setFont(part.isHighlighted ? codeFont : basicFont, fontSize);
                contentStream.setNonStrokingColor(part.isHighlighted ? new Color(0, 150, 0) : Color.BLACK);
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

        // Calculate total width based on page width
        float tableWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        float rowHeight = 20;
        float tableHeight = tableData.size() * rowHeight;

        // Check if we need a new page
        if (yPosition - tableHeight < MARGIN) {
            contentStream.close();
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            yPosition = Y_START;
        }

        // Calculate column widths
        float[] actualColumnWidths = new float[columnWidths.length];
        float sum = 0;
        for (float width : columnWidths) {
            sum += width;
        }
        for (int i = 0; i < columnWidths.length; i++) {
            actualColumnWidths[i] = (columnWidths[i] / sum) * tableWidth;
        }
        // Draw table
        float y = yPosition;
        for (String[] row : tableData) {
            float x = MARGIN;
            // Draw cell backgrounds
            for (int i = 0; i < actualColumnWidths.length && i < row.length; i++) {
                contentStream.setNonStrokingColor(new Color(240, 240, 240));
                contentStream.addRect(x, y - rowHeight, actualColumnWidths[i], rowHeight);
                contentStream.fill();
                contentStream.setStrokingColor(Color.LIGHT_GRAY);
                contentStream.setLineWidth(0.5f);
                contentStream.addRect(x, y - rowHeight, actualColumnWidths[i], rowHeight);
                contentStream.stroke();
                x += actualColumnWidths[i];
            }
            // Draw cell text
            x = MARGIN;
            for (int i = 0; i < actualColumnWidths.length && i < row.length; i++) {
                contentStream.beginText();
                contentStream.setFont(basicFont, 9);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(x + 5, y - rowHeight + 7); // Add some padding
                contentStream.showText(row[i]);
                contentStream.endText();
                x += actualColumnWidths[i];
            }

            y -= rowHeight;
        }
        yPosition = y;
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
