package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Mapper.ApplicationMapper;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.CandidateProfileRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowStageRepository;

import java.time.OffsetDateTime;

@Service
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final JobPostingRepository postingRepo;
    private final WorkflowStageRepository stageRepo;
    private final CandidateProfileRepository candidateRepo;

    public ApplicationService(ApplicationRepository appRepo,JobPostingRepository postingRepo,
                              WorkflowStageRepository stageRepo,CandidateProfileRepository candidateRepo) {
        this.appRepo = appRepo;
        this.postingRepo = postingRepo;
        this.stageRepo = stageRepo;
        this.candidateRepo = candidateRepo;
    }
    @Transactional
    public ApplicationDTO apply(Long postingId, Long candidateId) {
        if (appRepo.existsByJobPosting_IdAndCandidate_Id(postingId, candidateId)) {
            throw new IllegalStateException("Already applied");
        }

        var posting = postingRepo.findByIdWithRequestion(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found " + postingId));

        var initial = stageRepo.findFirstByWorkflow_IdOrderBySortOrderAsc(
                posting.getPipelineWorkflow().getId());

        var workflow = posting.getPipelineWorkflow();

        var a = new Application();
        a.setJobPosting(posting);
        a.setCandidate(candidateRepo.getReferenceById(candidateId));
        a.setWorkflow(workflow);
        a.setCurrentStage(initial);
        a.setStatus(ApplicationStatus.ACTIVE);
        a.setAppliedAt(OffsetDateTime.now());

        return ApplicationMapper.toDto(appRepo.save(a));

    }
}
