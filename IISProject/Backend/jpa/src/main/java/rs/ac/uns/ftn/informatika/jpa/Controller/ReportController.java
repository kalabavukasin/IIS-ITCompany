package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.ReportDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.ReportService;
import rs.ac.uns.ftn.informatika.jpa.Service.PdfReportService;

import java.time.LocalDate;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = Logger.getLogger(ReportController.class.getName());

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping(value = "/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            ReportDTO report = reportService.generateReport(startDate, endDate);
            byte[] pdfContent = pdfReportService.generatePdfReport(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("izvestaj_%s_do_%s.pdf", 
                    startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            logger.severe("Error generating custom PDF report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/pdf/current-month", produces = "application/pdf")
    public ResponseEntity<byte[]> generateCurrentMonthPdfReport() {
        try {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now();
            
            ReportDTO report = reportService.generateReport(startOfMonth, endOfMonth);
            byte[] pdfContent = pdfReportService.generatePdfReport(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("izvestaj_%s.pdf", 
                    startOfMonth.format(java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"))));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            logger.severe("Error generating current month PDF report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/pdf/current-year", produces = "application/pdf")
    public ResponseEntity<byte[]> generateCurrentYearPdfReport() {
        try {
            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now();
            
            ReportDTO report = reportService.generateReport(startOfYear, endOfYear);
            byte[] pdfContent = pdfReportService.generatePdfReport(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("izvestaj_%d.pdf", LocalDate.now().getYear()));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            logger.severe("Error generating current year PDF report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/pdf/last-30-days", produces = "application/pdf")
    public ResponseEntity<byte[]> generateLast30DaysPdfReport() {
        try {
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            LocalDate today = LocalDate.now();
            
            ReportDTO report = reportService.generateReport(thirtyDaysAgo, today);
            byte[] pdfContent = pdfReportService.generatePdfReport(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "izvestaj_poslednjih_30_dana.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            logger.severe("Error generating last 30 days PDF report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}