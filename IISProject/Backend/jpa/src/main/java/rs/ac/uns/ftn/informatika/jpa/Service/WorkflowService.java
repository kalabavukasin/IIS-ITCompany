package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowStage;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowStageRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class WorkflowService {
    private final ApplicationRepository applicationRepository;
    private final WorkflowStageRepository stageRepository;

    public WorkflowService(ApplicationRepository applicationRepository,
                           WorkflowStageRepository stageRepository) {
        this.applicationRepository = applicationRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public List<String> listStageNamesForApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        Long defId = resolveWorkflowDefId(app);
        List<WorkflowStage> stages = stageRepository.findAllOrderedByDefId(defId);

        return stages.stream()
                .sorted(Comparator.comparingInt(this::safeIndex))
                .map(WorkflowStage::getName)
                .toList();
    }

    @Transactional
    public void advanceToNextStage(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        Long defId = resolveWorkflowDefId(app);
        List<WorkflowStage> stages = stageRepository.findAllOrderedByDefId(defId);
        WorkflowStage next = nextStageLinear(stages, app.getCurrentStage());
        if (next == null) {
            // nema sledeće faze – opcionalno postavi finalni status
            // app.setStatus(ApplicationStatus.COMPLETED);
            return;
        }
        app.setCurrentStage(next);
        applicationRepository.save(app);
    }

    @Transactional
    public void refuse(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        app.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(app);
    }

    // ====================== helperi ======================

    @Transactional
    public Long resolveWorkflowDefId(Application app) {
        if (app.getCurrentStage() != null && app.getCurrentStage().getWorkflow() != null) {
            return app.getCurrentStage().getWorkflow().getId();
        }
        // if (app.getJobPosting() != null && app.getJobPosting().getWorkflowDef() != null) {
        //     return app.getJobPosting().getWorkflowDef().getId();
        // }
        // if (app.getJobPosting() != null && app.getJobPosting().getRequestion() != null
        //     && app.getJobPosting().getRequestion().getWorkflowDef() != null) {
        //     return app.getJobPosting().getRequestion().getWorkflowDef().getId();
        // }
        throw new IllegalStateException("Workflow definition not resolvable for application " + app.getId());
    }

    private WorkflowStage nextStageLinear(List<WorkflowStage> ordered, WorkflowStage current) {
        if (ordered == null || ordered.isEmpty()) return null;
        if (current == null) return ordered.get(0); // ako nema tekuće faze, start je prva

        int idx = indexOfStage(ordered, current);
        if (idx < 0) return ordered.get(0);
        if (idx + 1 >= ordered.size()) return null;
        return ordered.get(idx + 1);
    }

    private int indexOfStage(List<WorkflowStage> ordered, WorkflowStage s) {
        for (int i = 0; i < ordered.size(); i++) {
            if (Objects.equals(ordered.get(i).getId(), s.getId())) return i;
        }
        return -1;
    }

    private int safeIndex(WorkflowStage s) {
        Integer i = s.getSortOrder();
        return i != null ? i : Integer.MAX_VALUE;
    }
}
