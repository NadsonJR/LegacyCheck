package org.legacyCheck.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;


public class PDFGenerator {

    // Method to generate a PDF file from the given content
    public void generatePDF(String content, String outputPath) {
        try {
            // Create a new document
            PdfWriter writer = new PdfWriter(outputPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Processa o conteúdo linha por linha
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.startsWith("#")) {
                    // Adiciona como cabeçalho
                    document.add(new Paragraph(line.substring(1).trim()).setNeutralRole().simulateBold()).setFontSize(16);
                } else if (line.startsWith("-")) {
                    // Adiciona como item de lista
                    List list = new List();
                    list.add(new ListItem(line.substring(1).trim()));
                    document.add(list);
                } else {
                    // Adiciona como parágrafo normal
                    document.add(new Paragraph(line.trim()));
                }
            }

            // Close the document
            document.close();
        } catch (Exception e) {
            System.out.println("Error generating PDF: " + e.getMessage());
        }
    }
}
