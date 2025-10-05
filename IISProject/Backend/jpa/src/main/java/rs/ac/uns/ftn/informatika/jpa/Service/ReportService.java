package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Dto.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.ApplicationStatusHistory;
import rs.ac.uns.ftn.informatika.jpa.Repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    
    @Autowired
    private OfferRepository offerRepository;

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
        
        // 3. Prosečno vreme do zaposlenja
        report.setAverageTimeToHire(getAverageTimeToHire(startDateTime, endDateTime));
        
        // 4. Prosečno vreme po fazi
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
        return applicationRepository.findApplicationsByDateRange(startDate, endDate).stream()
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
        List<ApplicationStatusHistory> histories = applicationStatusHistoryRepository.findHistoriesByDateRange(startDate, endDate);
        
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
        List<Application> hiredApplications = applicationRepository.findApplicationsByStatusAndDateRange(ApplicationStatus.HIRED, startDate, endDate);
        
        if (hiredApplications.isEmpty()) {
            return 0.0;
        }
        
        double totalDays = hiredApplications.stream()
                .mapToDouble(app -> {
                    // Pronađi poslednji status history za ovu aplikaciju
                    List<ApplicationStatusHistory> histories = applicationStatusHistoryRepository
                            .findByApplication_IdOrderByEnteredAtAsc(app.getId());
                    
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
        List<ApplicationStatusHistory> histories = applicationStatusHistoryRepository.findCompletedHistoriesByDateRange(startDate, endDate);
        
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
        List<Application> applications = applicationRepository.findApplicationsByDateRange(startDate, endDate);
        
        long totalInvited = applications.size();
        long totalRejected = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .count();
        
        double rejectionRate = totalInvited > 0 ? (double) totalRejected / totalInvited * 100 : 0.0;
        
        return new InvitationRejectionRatioDTO(totalInvited, totalRejected, rejectionRate);
    }

    private Double getOfferRejectionPercentage(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<rs.ac.uns.ftn.informatika.jpa.Model.Offer> offers = offerRepository.findOffersByDateRange(startDate, endDate);
        
        if (offers.isEmpty()) {
            return 0.0;
        }
        
        long declinedOffers = offers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.DECLINED)
                .count();
        
        return (double) declinedOffers / offers.size() * 100;
    }

    private Long getTotalApplications(OffsetDateTime startDate, OffsetDateTime endDate) {
        return (long) applicationRepository.findApplicationsByDateRange(startDate, endDate).size();
    }

    private Long getTotalHired(OffsetDateTime startDate, OffsetDateTime endDate) {
        return (long) applicationRepository.findApplicationsByStatusAndDateRange(ApplicationStatus.HIRED, startDate, endDate).size();
    }
}
