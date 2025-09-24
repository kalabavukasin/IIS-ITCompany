package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.*;

import java.time.OffsetDateTime;

public class ApplicationDTO {
    public Long id;

    public Long jobPostingId;
    public Long candidateProfileId;
    public Long workflowDefId;
    public Long workflowStageId;

    public String status;
    public OffsetDateTime createdAt;

}
