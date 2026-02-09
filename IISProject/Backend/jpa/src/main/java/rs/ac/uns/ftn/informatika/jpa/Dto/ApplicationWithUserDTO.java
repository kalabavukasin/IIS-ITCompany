package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ApplicationWithUserDTO {
    public Long applicationId;
    public String status;
    public String currentPhase;

    public Long jobPostingId;
    public String requestName;
    public String requestDescription;
    public String requestLocation;

    public Long candidateId;
    public String candidateName;
    public LocalDate openUntil;

    public ApplicationWithUserDTO(
            Long applicationId,
            String status,
            String currentPhase,
            Long jobPostingId,
            String requestName,
            String requestDescription,
            String requestLocation,
            Long candidateId,
            String candidateName,
            LocalDate openUntil) {
        this.applicationId = applicationId;
        this.status = status;
        this.currentPhase = currentPhase;
        this.jobPostingId = jobPostingId;
        this.requestName = requestName;
        this.requestDescription = requestDescription;
        this.requestLocation = requestLocation;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.openUntil = openUntil;
    }
}
