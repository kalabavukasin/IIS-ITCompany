package rs.ac.uns.ftn.informatika.jpa.Dto;

public class StageConversionDTO {
    private String stageName;
    private Long enteredCount;
    private Long completedCount;
    private Double conversionRate;
    
    public StageConversionDTO() {}
    
    public StageConversionDTO(String stageName, Long enteredCount, Long completedCount, Double conversionRate) {
        this.stageName = stageName;
        this.enteredCount = enteredCount;
        this.completedCount = completedCount;
        this.conversionRate = conversionRate;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public Long getEnteredCount() {
        return enteredCount;
    }
    
    public void setEnteredCount(Long enteredCount) {
        this.enteredCount = enteredCount;
    }
    
    public Long getCompletedCount() {
        return completedCount;
    }
    
    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }
    
    public Double getConversionRate() {
        return conversionRate;
    }
    
    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }
}
