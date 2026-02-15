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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = Logger.getLogger(ReportController.class.getName());

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    public ReportController(ReportService reportService, PdfReportService pdfReportService) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
    }

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

    // ===== PL/SQL INTEGRATED ENDPOINTS =====
    

    // Generise izvestaj koristeći PL/SQL funkcije
    @GetMapping(value = "/plsql", produces = "application/json")
    public ResponseEntity<ReportDTO> generatePlSqlReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            ReportDTO report = reportService.generateReportWithPlSql(startDate, endDate);
            return ResponseEntity.ok(report);
                    
        } catch (Exception e) {
            logger.severe("Error generating PL/SQL report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    //Generise kompleksan PL/SQL izvestaj

    @GetMapping(value = "/plsql/comprehensive", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> generateComprehensivePlSqlReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Map<String, Object>> report = reportService.generateComprehensivePlSqlReport(startDate, endDate);
            return ResponseEntity.ok(report);
                    
        } catch (Exception e) {
            logger.severe("Error generating comprehensive PL/SQL report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Testira performanse indeksa
    @GetMapping(value = "/plsql/performance-test", produces = "application/json")
    public ResponseEntity<Map<String, Object>> testIndexPerformance() {
        try {
            Map<String, Object> result = reportService.testIndexPerformance();
            return ResponseEntity.ok(result);
                    
        } catch (Exception e) {
            logger.severe("Error testing index performance: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Postavlja trenutnog korisnika za audit log
    @PostMapping(value = "/plsql/set-current-user/{userId}")
    public ResponseEntity<String> setCurrentUserForAudit(@PathVariable Long userId) {
        try {
            reportService.setCurrentUserForAudit(userId);
            return ResponseEntity.ok("Current user set to: " + userId);
                    
        } catch (Exception e) {
            logger.severe("Error setting current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}