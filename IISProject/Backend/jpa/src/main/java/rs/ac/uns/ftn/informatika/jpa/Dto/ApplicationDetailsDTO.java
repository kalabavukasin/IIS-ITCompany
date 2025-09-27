package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ApplicationDetailsDTO (
        Long applicationId,
        String applicationStatus,
        OffsetDateTime appliedAt,

        // Job/Requestion
        Long jobPostingId,
        String requestName,
        String requestDescription,
        String requestLocation,
        String seniority,
        BigDecimal salary,
        String technologies,
        String createdBy,
        LocalDate openUntil,

        // Candidate
        Long candidateId,
        String candidateFullName,
        String candidateEmail,
        String candidatePhone,
        String cvDownloadUrl,         // link do controller-a koji servira CV

        // Workflow
        String currentPhase,
        List<String> phases,           //["Preselekcija","Test","Intervju","Ponuda"]
        String comment
)
{ }