package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationWithUserDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Mapper.ApplicationMapper;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.CandidateProfileRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowStageRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final JobPostingRepository postingRepo;
    private final WorkflowStageRepository stageRepo;
    private final CandidateProfileRepository candidateRepo;
    private final WorkflowService workflowService;

    public ApplicationService(ApplicationRepository appRepo,JobPostingRepository postingRepo,
                              WorkflowStageRepository stageRepo,CandidateProfileRepository candidateRepo,
                              WorkflowService workflowService) {
        this.appRepo = appRepo;
        this.postingRepo = postingRepo;
        this.stageRepo = stageRepo;
        this.candidateRepo = candidateRepo;
        this.workflowService = workflowService;
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
    @Transactional
    public List<ApplicationDTO> listMine(Long candidateId){
        return appRepo.findByCandidate_IdOrderByAppliedAtDesc(candidateId)
                .stream().map(ApplicationMapper::toDto).toList();
    }
    public List<ApplicationCardDTO> getMyApplicationCards(Long candidateId) {
        return appRepo.findCardsByCandidateId(candidateId);
    }
    public List<ApplicationWithUserDTO> getAllCards() {
        return appRepo.findAllCards();
    }
    @Transactional
    public ApplicationDetailsDTO getDetails(Long appId) {
        var raw = appRepo.findRawDetails(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

        // 1) CV download URL (npr. /api/cv/{candidateId})
        String cvUrl = null;
        var prof = candidateRepo.findById(raw.candidateId());
        if (prof.isPresent() && prof.get().getCvPath() != null) {
            cvUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/cv/")
                    .path(prof.get().getCvPath())
                    .toUriString();
        }

        List<String> phases = workflowService.listStageNamesForApplication(appId);

        return new ApplicationDetailsDTO(
                raw.applicationId(),
                raw.applicationStatus(),
                raw.appliedAt(),
                raw.jobPostingId(),
                raw.requestName(),
                raw.requestDescription(),
                raw.requestLocation(),
                raw.seniority(),
                raw.salary(),
                raw.technologies(),
                raw.createdBy(),
                raw.openUntil(),
                raw.candidateId(),
                raw.candidateFullName(),
                raw.candidateEmail(),
                raw.candidatePhone(),
                cvUrl,
                raw.currentPhase(),
                phases
        );
    }
}
