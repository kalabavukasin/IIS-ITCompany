package rs.ac.uns.ftn.informatika.jpa.Mapper;

import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;

public class ApplicationMapper {
    /*public static Application toEntity(ApplicationDTO dto) {
        Application a = new Application();
        a.setWorkflow(dto.workflowDef);
        a.setCurrentStage(dto.workflowStage);
        a.setAppliedAt(dto.createdAt);
        a.setStatus(dto.status);
        a.setCandidate(dto.candidateProfile);
        a.setJobPosting(dto.jobPosting);
        a.setId(dto.id);
        return a;
    }*/
    public static ApplicationDTO toDto(Application a) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.id = a.getId();
        dto.jobPostingId = (a.getJobPosting() != null) ? a.getJobPosting().getId() : null;
        dto.candidateProfileId = (a.getCandidate() != null) ? a.getCandidate().getId() : null;
        dto.workflowDefId = (a.getWorkflow() != null) ? a.getWorkflow().getId() : null;
        dto.workflowStageId = (a.getCurrentStage() != null) ? a.getCurrentStage().getId() : null;
        dto.status = (a.getStatus() != null) ? a.getStatus().name() : null;
        dto.createdAt = a.getAppliedAt();
        return dto;
    }
}
