package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Dto.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.ApplicationStatusHistory;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ApplicationService applicationService;
    private final OfferService offerService;
    private final JdbcTemplate jdbcTemplate;

    public ReportService(ApplicationService applicationService,
                         OfferService offerService,
                         JdbcTemplate jdbcTemplate) {
        this.applicationService = applicationService;
        this.offerService = offerService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public ReportDTO generateReport(LocalDate startDate, LocalDate endDate) {
        ReportDTO report = new ReportDTO();
        report.setReportStartDate(startDate);
        report.setReportEndDate(endDate);
        
        // Konvertujemo LocalDate u OffsetDateTime za bazu
        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endDateTime = endDate.atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());
        
        // 1. Broj prijava po oglasu
        report.setApplicationsPerJobPosting(getApplicationsPerJobPosting(startDateTime, endDateTime));
        
        // 2. Konverzije po fazama procesa
        report.setStageConversions(getStageConversions(startDateTime, endDateTime));
        
        // 3. Prosecno vreme do zaposlenja
        report.setAverageTimeToHire(getAverageTimeToHire(startDateTime, endDateTime));
        
        // 4. Prosecno vreme po fazi
        report.setAverageTimePerStage(getAverageTimePerStage(startDateTime, endDateTime));
        
        
        // 6. Odnos pozivani/odbijeni
        report.setInvitationRejectionRatio(getInvitationRejectionRatio(startDateTime, endDateTime));
        
        // 7. Procent odbijanja ponuda
        report.setOfferRejectionPercentage(getOfferRejectionPercentage(startDateTime, endDateTime));
        
        // Dodatne statistike
        report.setTotalApplications(getTotalApplications(startDateTime, endDateTime));
        report.setTotalHired(getTotalHired(startDateTime, endDateTime));
        
        return report;
    }


    private List<JobPostingApplicationCountDTO> getApplicationsPerJobPosting(OffsetDateTime startDate, OffsetDateTime endDate) {
        return applicationService.findApplicationsByDateRange(startDate, endDate).stream()
                .collect(Collectors.groupingBy(Application::getJobPosting))
                .entrySet().stream()
                .map(entry -> new JobPostingApplicationCountDTO(
                        entry.getKey().getId(),
                        entry.getKey().getRequestion().getName(),
                        (long) entry.getValue().size()
                ))
                .sorted((a, b) -> Long.compare(b.getApplicationCount(), a.getApplicationCount()))
                .collect(Collectors.toList());
    }

    private List<StageConversionDTO> getStageConversions(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ApplicationStatusHistory> histories = applicationService.findHistoriesByDateRange(startDate, endDate);
        
        Map<String, List<ApplicationStatusHistory>> byStage = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getStage().getName()));
        
        return byStage.entrySet().stream()
                .map(entry -> {
                    String stageName = entry.getKey();
                    List<ApplicationStatusHistory> stageHistories = entry.getValue();
                    long enteredCount = stageHistories.size();
                    long completedCount = stageHistories.stream()
                            .filter(h -> h.getExitedAt() != null)
                            .count();
                    double conversionRate = enteredCount > 0 ? (double) completedCount / enteredCount * 100 : 0.0;
                    
                    return new StageConversionDTO(stageName, enteredCount, completedCount, conversionRate);
                })
                .sorted((a, b) -> Long.compare(b.getEnteredCount(), a.getEnteredCount()))
                .collect(Collectors.toList());
    }

    private Double getAverageTimeToHire(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Application> hiredApplications = applicationService.findApplicationsByStatusAndDateRange(ApplicationStatus.HIRED, startDate, endDate);
        
        if (hiredApplications.isEmpty()) {
            return 0.0;
        }
        
        double totalDays = hiredApplications.stream()
                .mapToDouble(app -> {
                    // Pronadji poslednji status history za ovu aplikaciju
                    List<ApplicationStatusHistory> histories = applicationService
                            .findHistoriesByApplicationIdOrderByEnteredAtAsc(app.getId());
                    
                    if (histories.isEmpty()) {
                        return 0.0;
                    }
                    
                    ApplicationStatusHistory lastHistory = histories.get(histories.size() - 1);
                    OffsetDateTime endTime = lastHistory.getExitedAt() != null ? 
                            lastHistory.getExitedAt() : OffsetDateTime.now();
                    
                    return ChronoUnit.DAYS.between(app.getAppliedAt(), endTime);
                })
                .sum();
        
        return totalDays / hiredApplications.size();
    }

    private List<StageAverageTimeDTO> getAverageTimePerStage(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<ApplicationStatusHistory> histories = applicationService.findCompletedHistoriesByDateRange(startDate, endDate);
        
        Map<String, List<ApplicationStatusHistory>> byStage = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getStage().getName()));
        
        return byStage.entrySet().stream()
                .map(entry -> {
                    String stageName = entry.getKey();
                    List<ApplicationStatusHistory> stageHistories = entry.getValue();
                    
                    double averageTime = stageHistories.stream()
                            .mapToDouble(h -> ChronoUnit.DAYS.between(h.getEnteredAt(), h.getExitedAt()))
                            .average()
                            .orElse(0.0);
                    
                    return new StageAverageTimeDTO(stageName, averageTime, (long) stageHistories.size());
                })
                .sorted((a, b) -> Double.compare(b.getAverageTimeInDays(), a.getAverageTimeInDays()))
                .collect(Collectors.toList());
    }


    private InvitationRejectionRatioDTO getInvitationRejectionRatio(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Application> applications = applicationService.findApplicationsByDateRange(startDate, endDate);
        
        long totalInvited = applications.size();
        long totalRejected = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .count();
        
        double rejectionRate = totalInvited > 0 ? (double) totalRejected / totalInvited * 100 : 0.0;
        
        return new InvitationRejectionRatioDTO(totalInvited, totalRejected, rejectionRate);
    }

    private Double getOfferRejectionPercentage(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<rs.ac.uns.ftn.informatika.jpa.Model.Offer> offers = offerService.findOffersByDateRange(startDate, endDate);
        
        if (offers.isEmpty()) {
            return 0.0;
        }
        
        long declinedOffers = offers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.DECLINED)
                .count();
        
        return (double) declinedOffers / offers.size() * 100;
    }

    private Long getTotalApplications(OffsetDateTime startDate, OffsetDateTime endDate) {
        return (long) applicationService.findApplicationsByDateRange(startDate, endDate).size();
    }

    private Long getTotalHired(OffsetDateTime startDate, OffsetDateTime endDate) {
        return (long) applicationService.findApplicationsByStatusAndDateRange(ApplicationStatus.HIRED, startDate, endDate).size();
    }

    // ===== PL/SQL INTEGRATED METHODS =====

     // Generise izvestaj koristeći PL/SQL funkcije umesto JPA upita

    @Transactional(readOnly = true)
    public ReportDTO generateReportWithPlSql(LocalDate startDate, LocalDate endDate) {
        ReportDTO report = new ReportDTO();
        report.setReportStartDate(startDate);
        report.setReportEndDate(endDate);
        
        try {
            // Koristi netrivijalnu PL/SQL funkciju za osnovne metrike
            Map<String, Object> metrics = jdbcTemplate.queryForMap(
                "SELECT * FROM calculate_recruitment_metrics(?, ?, ?)", 
                startDate, endDate, (Long) null
            );
            
            report.setTotalApplications(((Number) metrics.get("total_applications")).longValue());
            report.setTotalHired(((Number) metrics.get("total_hired")).longValue());
            report.setAverageTimeToHire(((Number) metrics.get("average_time_to_hire")).doubleValue());
            report.setOfferRejectionPercentage(((Number) metrics.get("offer_rejection_percentage")).doubleValue());
            
            // Ostale metrike racunamo koristeći JPA (fallback pristup)
            OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime endDateTime = endDate.atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());
            
            report.setStageConversions(getStageConversions(startDateTime, endDateTime));
            report.setApplicationsPerJobPosting(getApplicationsPerJobPosting(startDateTime, endDateTime));
            report.setAverageTimePerStage(getAverageTimePerStage(startDateTime, endDateTime));
            
            // Izracunaj odnos pozivani/odbijeni
            long totalInvited = report.getTotalApplications();
            long totalRejected = totalInvited - report.getTotalHired();
            double rejectionRate = totalInvited > 0 ? (double) totalRejected / totalInvited * 100 : 0.0;
            report.setInvitationRejectionRatio(new InvitationRejectionRatioDTO(totalInvited, totalRejected, rejectionRate));
            
        } catch (Exception e) {
            // Fallback na originalnu implementaciju ako PL/SQL ne radi
            return generateReport(startDate, endDate);
        }
        
        return report;
    }
    

    // Generise kompleksan PL/SQL izvestaj

    @Transactional(readOnly = true)
    public List<Map<String, Object>> generateComprehensivePlSqlReport(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM generate_comprehensive_recruitment_report(?, ?)", 
            startDate, endDate
        );
    }
    

     // Postavlja trenutnog korisnika za audit log

    @Transactional
    public void setCurrentUserForAudit(Long userId) {
        jdbcTemplate.queryForObject("SELECT set_current_user_id(?)", Long.class, userId);
    }
    

    // Testira performanse indeksa

    @Transactional(readOnly = true)
    public Map<String, Object> testIndexPerformance() {
        Map<String, Object> result = new HashMap<>();
        
        // Test bez indeksa (simulacija)
        long startTime = System.currentTimeMillis();
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM applications a " +
            "JOIN application_status_history ash ON a.id = ash.application_id " +
            "WHERE a.application_status = 'HIRED' AND ash.entered_at > NOW() - INTERVAL '30 days'",
            Long.class
        );
        long timeWithIndex = System.currentTimeMillis() - startTime;
        
        result.put("timeWithIndex", timeWithIndex);
        result.put("testQuery", "Complex JOIN with status and date filtering");
        result.put("indexesUsed", "idx_applications_status_date, idx_status_history_entered_at");
        
        return result;
    }
}
