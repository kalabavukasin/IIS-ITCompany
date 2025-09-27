package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rs.ac.uns.ftn.informatika.jpa.Dto.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;
import rs.ac.uns.ftn.informatika.jpa.Mapper.ApplicationMapper;
import rs.ac.uns.ftn.informatika.jpa.Model.*;
import rs.ac.uns.ftn.informatika.jpa.Repository.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final JobPostingRepository postingRepo;
    private final WorkflowStageRepository stageRepo;
    private final CandidateProfileRepository candidateRepo;
    private final WorkflowService workflowService;
    private final ApplicationRepository applicationRepository;
    private final WorkflowTransitionRepository transitionRepo;
    private final ApplicationStatusHistoryRepository historyRepo;
    private final UserService userService;
    private final OfferRepository offerRepo;

    public ApplicationService(ApplicationRepository appRepo, JobPostingRepository postingRepo,
                              WorkflowStageRepository stageRepo, CandidateProfileRepository candidateRepo,
                              WorkflowService workflowService, ApplicationRepository applicationRepository,
                              WorkflowTransitionRepository transitionRepo,
                              ApplicationStatusHistoryRepository historyRepo,
                              UserService userService,
                              OfferRepository offerRepo) {
        this.appRepo = appRepo;
        this.postingRepo = postingRepo;
        this.stageRepo = stageRepo;
        this.candidateRepo = candidateRepo;
        this.workflowService = workflowService;
        this.applicationRepository = applicationRepository;
        this.transitionRepo = transitionRepo;
        this.historyRepo = historyRepo;
        this.userService = userService;
        this.offerRepo = offerRepo;
    }
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
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

        // 1) CV download URL
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
    @Transactional
    public ApplicationDTO refuse(Long applicationId, String reason) {

        Application application = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found " + applicationId));


        application.setNote(reason);
        application.setStatus(ApplicationStatus.REJECTED);


        return ApplicationMapper.toDto(appRepo.save(application));

    }
    @Transactional
    public boolean advanceWorkflow(Long applicationId, String comment, Long triggeredByUserId, Role userRole) {
        Application app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (app.getStatus() != ApplicationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot advance workflow for non-active application");
        }

        WorkflowStage currentStage = app.getCurrentStage();
        if (currentStage == null) {
            throw new IllegalStateException("Application has no current stage");
        }

        // Find possible transitions from current stage
        List<WorkflowTransition> transitions = transitionRepo.findByFromStageId(currentStage.getId());
        if (transitions.isEmpty()) {
            // No more transitions - we're at the final stage
            return false;
        }

        // For linear workflow, there should be only one transition
        WorkflowTransition transition = transitions.get(0);

        // Check if user has permission for this transition
        if (userRole != null && !transition.getAllowedRoles().contains(userRole)) {
            throw new SecurityException("User role " + userRole + " is not allowed for this transition");
        }

        WorkflowStage nextStage = transition.getToStage();

        // Update the last history entry's exitedAt
        updateLastHistoryExit(app, OffsetDateTime.now());

        // Create new history entry
        createHistoryEntry(app, nextStage, transition, comment, triggeredByUserId);

        // Update application's current stage
        app.setCurrentStage(nextStage);
        appRepo.save(app);

        return true;
    }


    @Transactional
    public void advanceWorkflowOnTestSent(Long applicationId, Long triggeredByUserId) {
        // When test is sent, we advance from Preselekcija to Test
        advanceWorkflow(applicationId, "Test sent to candidate", triggeredByUserId, Role.HR_MANAGER);
    }

    //Kada se intervju zakaze , ovo se salje
    @Transactional
    public void advanceWorkflowOnInterviewScheduled(Long applicationId, Long triggeredByUserId) {
        // Advance from Test to Intervju
        advanceWorkflow(applicationId, "Interview scheduled", triggeredByUserId, Role.HR_MANAGER);
    }

    //Kada se posalje ponuda , ovo se salje
    @Transactional
    public void advanceWorkflowOnOfferMade(Long applicationId, Long triggeredByUserId) {
        // Advance from Intervju to Ponuda
        advanceWorkflow(applicationId, "Offer made to candidate", triggeredByUserId, Role.HR_MANAGER);
    }

    private void createInitialHistoryEntry(Application app, WorkflowStage initialStage) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setStage(initialStage);
        history.setTransition(null); // No transition for initial entry
        history.setEnteredAt(app.getAppliedAt());
        history.setExitedAt(null);
        history.setComment("Application submitted");
        history.setTriggeredBy(null); // Candidate triggered by applying

        historyRepo.save(history);
    }

    private void createHistoryEntry(Application app, WorkflowStage stage,
                                    WorkflowTransition transition, String comment, Long triggeredByUserId) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setStage(stage);
        history.setTransition(transition);
        history.setEnteredAt(OffsetDateTime.now());
        history.setExitedAt(null);
        history.setComment(comment);

        if (triggeredByUserId != null) {
            User user =  userService.getUserById(triggeredByUserId).
                     orElseThrow(() -> new RuntimeException("Application not found " + triggeredByUserId));
            history.setTriggeredBy(user);
        }

        historyRepo.save(history);
    }

    private void updateLastHistoryExit(Application app, OffsetDateTime exitTime) {
        // Find the last history entry (the one with exitedAt = null)
        Optional<ApplicationStatusHistory> lastHistory = historyRepo
                .findTopByApplicationAndExitedAtIsNullOrderByEnteredAtDesc(app);

        if (lastHistory.isPresent()) {
            ApplicationStatusHistory history = lastHistory.get();
            history.setExitedAt(exitTime);
            historyRepo.save(history);
        }
    }
    @Transactional
    public void createOffer(OfferCreateDTO dto, Long triggeredById) {
        Application app = appRepo.findById(dto.applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        Offer offer = new Offer();
        offer.setApplication(app);
        offer.setStartDate(dto.startDate);
        offer.setCreatedAt(OffsetDateTime.now());
        offer.setStatus(OfferStatus.SENT);
        offerRepo.save(offer);

        advanceWorkflowOnOfferMade(dto.applicationId, triggeredById);
    }
}
