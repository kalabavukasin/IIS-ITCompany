package rs.ac.uns.ftn.informatika.jpa.Dto;

public class JobPostingApplicationCountDTO {
    private Long jobPostingId;
    private String jobTitle;
    private Long applicationCount;
    
    public JobPostingApplicationCountDTO() {}
    
    public JobPostingApplicationCountDTO(Long jobPostingId, String jobTitle, Long applicationCount) {
        this.jobPostingId = jobPostingId;
        this.jobTitle = jobTitle;
        this.applicationCount = applicationCount;
    }
    
    public Long getJobPostingId() {
        return jobPostingId;
    }
    
    public void setJobPostingId(Long jobPostingId) {
        this.jobPostingId = jobPostingId;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public Long getApplicationCount() {
        return applicationCount;
    }
    
    public void setApplicationCount(Long applicationCount) {
        this.applicationCount = applicationCount;
    }
}
