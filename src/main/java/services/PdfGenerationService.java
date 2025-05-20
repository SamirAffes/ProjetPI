package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import entities.Maintenance;
import entities.Vehicule;
import entities.StatusMaintenance;
import entities.TypeMaintenance;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating PDF reports
 */
@Slf4j
public class PdfGenerationService {
    
    /**
     * Generates a PDF report for a maintenance record
     * 
     * @param maintenance The maintenance record
     * @param vehicule The vehicle associated with the maintenance
     * @return The path to the generated PDF file, or null if generation failed
     */
    public String generateMaintenanceReport(Maintenance maintenance, Vehicule vehicule) {
        try {
            // Create directory for reports if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            // Create a unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "maintenance_" + maintenance.getId() + "_" + timestamp + ".pdf";
            String filePath = "reports/" + filename;
            
            // Create document
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Add header
            addHeader(document, "Rapport de Maintenance");
            
            // Add vehicle information
            addVehicleInfo(document, vehicule);
            
            // Add maintenance details
            addMaintenanceDetails(document, maintenance);
            
            // Add footer
            addFooter(document);
            
            document.close();
            log.info("PDF report generated successfully: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            return null;
        }
    }
    
    /**
     * Adds a header to the document
     */
    private void addHeader(Document document, String title) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph header = new Paragraph(title, titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(20);
        document.add(header);
        
        // Add date
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
        Paragraph date = new Paragraph("Date du rapport: " + 
            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20);
        document.add(date);
        
        // Add separator
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }
    
    /**
     * Adds vehicle information to the document
     */
    private void addVehicleInfo(Document document, Vehicule vehicule) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Informations du Véhicule", sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(10);
        document.add(section);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Set column widths
        float[] columnWidths = {1f, 2f};
        table.setWidths(columnWidths);
        
        // Add table headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
        headerCell.setPadding(5);
        
        headerCell.setPhrase(new Phrase("Attribut", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Valeur", headerFont));
        table.addCell(headerCell);
        
        // Add table rows
        addTableRow(table, "Marque", vehicule.getMarque());
        addTableRow(table, "Modèle", vehicule.getModele());
        addTableRow(table, "Immatriculation", vehicule.getImmatriculation());
        addTableRow(table, "Type", vehicule.getType().toString());
        addTableRow(table, "Capacité", String.valueOf(vehicule.getCapacite()));
        addTableRow(table, "Date de fabrication", formatDate(vehicule.getDateFabrication()));
        
        document.add(table);
    }
    
    /**
     * Adds maintenance details to the document
     */
    private void addMaintenanceDetails(Document document, Maintenance maintenance) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Détails de la Maintenance", sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(10);
        document.add(section);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Set column widths
        float[] columnWidths = {1f, 2f};
        table.setWidths(columnWidths);
        
        // Add table headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
        headerCell.setPadding(5);
        
        headerCell.setPhrase(new Phrase("Attribut", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Valeur", headerFont));
        table.addCell(headerCell);
        
        // Add table rows
        addTableRow(table, "ID", String.valueOf(maintenance.getId()));
        addTableRow(table, "Type de maintenance", maintenance.getTypeMaintenance().toString());
        addTableRow(table, "Date de début", formatDate(maintenance.getDateDebut()));
        addTableRow(table, "Date de fin", formatDate(maintenance.getDateFin()));
        addTableRow(table, "Statut", getStatusWithColor(maintenance.getStatus()));
        
        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        addTableRow(table, "Coût", currencyFormat.format(maintenance.getPrix()));
        
        document.add(table);
        
        // Add description
        Font descFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph descTitle = new Paragraph("Description:", descFont);
        descTitle.setSpacingBefore(10);
        descTitle.setSpacingAfter(5);
        document.add(descTitle);
        
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        Paragraph description = new Paragraph(maintenance.getDescription(), normalFont);
        description.setSpacingAfter(10);
        document.add(description);
    }
    
    /**
     * Adds a footer to the document
     */
    private void addFooter(Document document) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph("Ce rapport a été généré automatiquement par le système TuniTransport.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);
    }
    
    /**
     * Adds a row to a table
     */
    private void addTableRow(PdfPTable table, String key, String value) {
        Font keyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        
        PdfPCell keyCell = new PdfPCell(new Phrase(key, keyFont));
        keyCell.setPadding(5);
        keyCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(keyCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    /**
     * Formats a date as a string
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
    
    /**
     * Gets the status with color coding
     */
    private String getStatusWithColor(StatusMaintenance status) {
        return status.toString();
    }
}