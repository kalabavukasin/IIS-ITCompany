package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;

public class ApplicationCardDTO {
    public Long applicationId;
    public ApplicationStatus status;
    public Long jobPostingId;
    public String requestName;
    public String requestDescription;

    public ApplicationCardDTO(Long applicationId,
                              ApplicationStatus status,
                              Long jobPostingId,
                              String requestName,
                              String requestDescription) {
        this.applicationId = applicationId;
        this.status = status;
        this.jobPostingId = jobPostingId;
        this.requestName = requestName;
        this.requestDescription = requestDescription;
    }
}
