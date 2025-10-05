package rs.ac.uns.ftn.informatika.jpa.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Dto.ReportDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    public byte[] generatePdfReport(ReportDTO report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Font setup
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Title
        Paragraph title = new Paragraph("IZVEŠTAJ O ZAPOŠLJAVANJU")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Report period
        Paragraph period = new Paragraph(String.format("Period: %s - %s", 
                report.getReportStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                report.getReportEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(period);

        // Summary statistics
        addSummarySection(document, report, font, boldFont);

        // Applications per job posting
        addApplicationsPerJobPostingSection(document, report, font, boldFont);

        // Stage conversions
        addStageConversionsSection(document, report, font, boldFont);

        // Average time per stage
        addAverageTimePerStageSection(document, report, font, boldFont);

        // Invitation rejection ratio
        addInvitationRejectionSection(document, report, font, boldFont);

        document.close();
        return outputStream.toByteArray();
    }

    private void addSummarySection(Document document, ReportDTO report, PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("UKUPNE STATISTIKE")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table summaryTable = new Table(2).useAllAvailableWidth();
        summaryTable.setMarginBottom(20);

        addSummaryRow(summaryTable, "Ukupno prijava:", report.getTotalApplications().toString(), font, boldFont);
        addSummaryRow(summaryTable, "Ukupno zaposlenih:", report.getTotalHired().toString(), font, boldFont);
        addSummaryRow(summaryTable, "Prosečno vreme do zaposlenja:", String.format("%.1f dana", report.getAverageTimeToHire()), font, boldFont);
        addSummaryRow(summaryTable, "Procent odbijanja ponuda:", String.format("%.1f%%", report.getOfferRejectionPercentage()), font, boldFont);

        document.add(summaryTable);
    }

    private void addApplicationsPerJobPostingSection(Document document, ReportDTO report, PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("BROJ PRIJAVA PO OGLASU")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (report.getApplicationsPerJobPosting().isEmpty()) {
            Paragraph noData = new Paragraph("Nema podataka za odabrani period")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(noData);
            return;
        }

        Table table = new Table(3).useAllAvailableWidth();
        table.setMarginBottom(20);

        // Header
        addTableHeader(table, "ID Oglasa", font, boldFont);
        addTableHeader(table, "Naziv pozicije", font, boldFont);
        addTableHeader(table, "Broj prijava", font, boldFont);

        // Data rows
        for (var item : report.getApplicationsPerJobPosting()) {
            addTableData(table, item.getJobPostingId().toString(), font);
            addTableData(table, item.getJobTitle(), font);
            addTableData(table, item.getApplicationCount().toString(), font);
        }

        document.add(table);
    }

    private void addStageConversionsSection(Document document, ReportDTO report, PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("KONVERZIJE PO FAZAMA PROCESA")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (report.getStageConversions().isEmpty()) {
            Paragraph noData = new Paragraph("Nema podataka za odabrani period")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(noData);
            return;
        }

        Table table = new Table(4).useAllAvailableWidth();
        table.setMarginBottom(20);

        // Header
        addTableHeader(table, "Naziv faze", font, boldFont);
        addTableHeader(table, "Broj ulazaka", font, boldFont);
        addTableHeader(table, "Broj završetaka", font, boldFont);
        addTableHeader(table, "Konverzija (%)", font, boldFont);

        // Data rows
        for (var item : report.getStageConversions()) {
            addTableData(table, item.getStageName(), font);
            addTableData(table, item.getEnteredCount().toString(), font);
            addTableData(table, item.getCompletedCount().toString(), font);
            addTableData(table, String.format("%.1f", item.getConversionRate()), font);
        }

        document.add(table);
    }

    private void addAverageTimePerStageSection(Document document, ReportDTO report, PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("PROSEČNO VREME PO FAZI")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (report.getAverageTimePerStage().isEmpty()) {
            Paragraph noData = new Paragraph("Nema podataka za odabrani period")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(noData);
            return;
        }

        Table table = new Table(3).useAllAvailableWidth();
        table.setMarginBottom(20);

        // Header
        addTableHeader(table, "Naziv faze", font, boldFont);
        addTableHeader(table, "Prosečno vreme (dana)", font, boldFont);
        addTableHeader(table, "Ukupno aplikacija", font, boldFont);

        // Data rows
        for (var item : report.getAverageTimePerStage()) {
            addTableData(table, item.getStageName(), font);
            addTableData(table, String.format("%.1f", item.getAverageTimeInDays()), font);
            addTableData(table, item.getTotalApplications().toString(), font);
        }

        document.add(table);
    }

    private void addInvitationRejectionSection(Document document, ReportDTO report, PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("ODNOS POZIVANI/ODBIJENI")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(3).useAllAvailableWidth();
        table.setMarginBottom(20);

        // Header
        addTableHeader(table, "Ukupno pozvanih", font, boldFont);
        addTableHeader(table, "Ukupno odbijenih", font, boldFont);
        addTableHeader(table, "Procenat odbijanja", font, boldFont);

        // Data
        addTableData(table, report.getInvitationRejectionRatio().getTotalInvited().toString(), font);
        addTableData(table, report.getInvitationRejectionRatio().getTotalRejected().toString(), font);
        addTableData(table, String.format("%.1f%%", report.getInvitationRejectionRatio().getRejectionRate()), font);

        document.add(table);
    }

    private void addSummaryRow(Table table, String label, String value, PdfFont font, PdfFont boldFont) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont));
        Cell valueCell = new Cell().add(new Paragraph(value).setFont(font));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(Table table, String text, PdfFont font, PdfFont boldFont) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(boldFont));
        cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
        table.addHeaderCell(cell);
    }

    private void addTableData(Table table, String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font));
        table.addCell(cell);
    }
}
