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
    public String seniority;

    public Long candidateId;
    public String candidateName;
    public LocalDate openUntil;

    public String cvDownloadUrl; // populated only for by-posting endpoint

    // AI scoring fields — null until scoring is run
    public Integer autoAiScore;
    public String autoAiScoreNote;
    public OffsetDateTime autoAiScoredAt;
    public Integer bulkAiScore;
    public String bulkAiScoreNote;
    public OffsetDateTime bulkAiScoredAt;

    public ApplicationWithUserDTO(
            Long applicationId,
            String status,
            String currentPhase,
            Long jobPostingId,
            String requestName,
            String requestDescription,
            String requestLocation,
            String seniority,
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
        this.seniority = seniority;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.openUntil = openUntil;
    }
}
