package rs.ac.uns.ftn.informatika.jpa.Dto;

public class StageAverageTimeDTO {
    private String stageName;
    private Double averageTimeInDays;
    private Long totalApplications;
    
    public StageAverageTimeDTO() {}
    
    public StageAverageTimeDTO(String stageName, Double averageTimeInDays, Long totalApplications) {
        this.stageName = stageName;
        this.averageTimeInDays = averageTimeInDays;
        this.totalApplications = totalApplications;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public Double getAverageTimeInDays() {
        return averageTimeInDays;
    }
    
    public void setAverageTimeInDays(Double averageTimeInDays) {
        this.averageTimeInDays = averageTimeInDays;
    }
    
    public Long getTotalApplications() {
        return totalApplications;
    }
    
    public void setTotalApplications(Long totalApplications) {
        this.totalApplications = totalApplications;
    }
}
