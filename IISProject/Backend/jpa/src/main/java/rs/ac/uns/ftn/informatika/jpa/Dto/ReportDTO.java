package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.LocalDate;
import java.util.List;

public class ReportDTO {
    
    // Broj prijava po oglasu
    private List<JobPostingApplicationCountDTO> applicationsPerJobPosting;
    
    // Konverzije po fazama procesa
    private List<StageConversionDTO> stageConversions;
    
    // Prosečno vreme do zaposlenja (u danima)
    private Double averageTimeToHire;
    
    // Prosečno vreme po fazi (u danima)
    private List<StageAverageTimeDTO> averageTimePerStage;
    
    
    // Odnos pozivani/odbijeni
    private InvitationRejectionRatioDTO invitationRejectionRatio;
    
    // Procent odbijanja ponuda
    private Double offerRejectionPercentage;
    
    // Period izveštaja
    private LocalDate reportStartDate;
    private LocalDate reportEndDate;
    
    // Ukupan broj aplikacija u periodu
    private Long totalApplications;
    
    // Ukupan broj zaposlenih u periodu
    private Long totalHired;
    
    public ReportDTO() {}
    
    // Getters and Setters
    public List<JobPostingApplicationCountDTO> getApplicationsPerJobPosting() {
        return applicationsPerJobPosting;
    }
    
    public void setApplicationsPerJobPosting(List<JobPostingApplicationCountDTO> applicationsPerJobPosting) {
        this.applicationsPerJobPosting = applicationsPerJobPosting;
    }
    
    public List<StageConversionDTO> getStageConversions() {
        return stageConversions;
    }
    
    public void setStageConversions(List<StageConversionDTO> stageConversions) {
        this.stageConversions = stageConversions;
    }
    
    public Double getAverageTimeToHire() {
        return averageTimeToHire;
    }
    
    public void setAverageTimeToHire(Double averageTimeToHire) {
        this.averageTimeToHire = averageTimeToHire;
    }
    
    public List<StageAverageTimeDTO> getAverageTimePerStage() {
        return averageTimePerStage;
    }
    
    public void setAverageTimePerStage(List<StageAverageTimeDTO> averageTimePerStage) {
        this.averageTimePerStage = averageTimePerStage;
    }
    
    
    public InvitationRejectionRatioDTO getInvitationRejectionRatio() {
        return invitationRejectionRatio;
    }
    
    public void setInvitationRejectionRatio(InvitationRejectionRatioDTO invitationRejectionRatio) {
        this.invitationRejectionRatio = invitationRejectionRatio;
    }
    
    public Double getOfferRejectionPercentage() {
        return offerRejectionPercentage;
    }
    
    public void setOfferRejectionPercentage(Double offerRejectionPercentage) {
        this.offerRejectionPercentage = offerRejectionPercentage;
    }
    
    public LocalDate getReportStartDate() {
        return reportStartDate;
    }
    
    public void setReportStartDate(LocalDate reportStartDate) {
        this.reportStartDate = reportStartDate;
    }
    
    public LocalDate getReportEndDate() {
        return reportEndDate;
    }
    
    public void setReportEndDate(LocalDate reportEndDate) {
        this.reportEndDate = reportEndDate;
    }
    
    public Long getTotalApplications() {
        return totalApplications;
    }
    
    public void setTotalApplications(Long totalApplications) {
        this.totalApplications = totalApplications;
    }
    
    public Long getTotalHired() {
        return totalHired;
    }
    
    public void setTotalHired(Long totalHired) {
        this.totalHired = totalHired;
    }
}
