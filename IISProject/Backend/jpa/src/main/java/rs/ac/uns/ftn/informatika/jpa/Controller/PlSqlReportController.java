package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Service.PlSqlReportService;
import rs.ac.uns.ftn.informatika.jpa.Service.PlSqlReportService.ReportSection;
import rs.ac.uns.ftn.informatika.jpa.Service.PlSqlReportService.PerformanceTestResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plsql-reports")
public class PlSqlReportController {

    private final PlSqlReportService plSqlReportService;

    public PlSqlReportController(PlSqlReportService plSqlReportService) {
        this.plSqlReportService = plSqlReportService;
    }

    // Generise kompleksan PL/SQL izvestaj
    @GetMapping("/comprehensive")
    public ResponseEntity<?> generateComprehensiveReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<ReportSection> report = plSqlReportService.generateComprehensiveReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Error in generateComprehensiveReport: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    // Izracunava osnovne metrike zaposljavanja

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getRecruitmentMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long jobPostingId) {
        
        try {
            Map<String, Object> metrics = plSqlReportService.calculateRecruitmentMetrics(startDate, endDate, jobPostingId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // Testira performanse indeksa

    @GetMapping("/performance-test")
    public ResponseEntity<PerformanceTestResult> testIndexPerformance() {
        try {
            PerformanceTestResult result = plSqlReportService.testIndexPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // Kreira test podatke za demonstraciju

    @PostMapping("/create-test-data")
    public ResponseEntity<String> createTestData() {
        try {
            plSqlReportService.createTestData();
            return ResponseEntity.ok("Test data created successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating test data: " + e.getMessage());
        }
    }


    // Postavlja trenutnog korisnika za audit log

    @PostMapping("/set-current-user/{userId}")
    public ResponseEntity<String> setCurrentUser(@PathVariable Long userId) {
        try {
            plSqlReportService.setCurrentUserId(userId);
            return ResponseEntity.ok("Current user set to: " + userId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error setting current user: " + e.getMessage());
        }
    }

    // Generise izvestaj za trenutni mesec

    @GetMapping("/current-month")
    public ResponseEntity<List<ReportSection>> getCurrentMonthReport() {
        try {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now();
            
            List<ReportSection> report = plSqlReportService.generateComprehensiveReport(startOfMonth, endOfMonth);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Generise izvestaj za poslednjih 30 dana
    // Test endpoint za proveru PL/SQL komponenti
    @GetMapping("/test-plsql")
    public ResponseEntity<?> testPlSqlComponents() {
        try {
            // Test osnovne funkcije
            Map<String, Object> metrics = plSqlReportService.calculateRecruitmentMetrics(
                LocalDate.now().minusDays(30), 
                LocalDate.now(), 
                null
            );
            return ResponseEntity.ok("PL/SQL functions work! Metrics: " + metrics);
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("PL/SQL Test Error: " + e.getMessage() + " - Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
        }
    }

    @GetMapping("/last-30-days")
    public ResponseEntity<?> getLast30DaysReport() {
        try {
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            LocalDate today = LocalDate.now();
            
            List<ReportSection> report = plSqlReportService.generateComprehensiveReport(thirtyDaysAgo, today);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Controller error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage() + " - Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
        }
    }
}
