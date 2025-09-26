package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Dto.WorkflowSummaryDto;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowDef;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowStage;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowDefRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowStageRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowService {
    private final ApplicationRepository applicationRepository;
    private final WorkflowStageRepository stageRepository;
    private final WorkflowDefRepository workflowDefRepository;

    public WorkflowService(ApplicationRepository applicationRepository,
                           WorkflowStageRepository stageRepository,
                           WorkflowDefRepository workflowDefRepository) {
        this.applicationRepository = applicationRepository;
        this.stageRepository = stageRepository;
        this.workflowDefRepository = workflowDefRepository;
    }

    public Optional<WorkflowDef> getWorkflowById(Long id) {
        return workflowDefRepository.findById(id);
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
    @Transactional(readOnly = true)
    public List<WorkflowSummaryDto> listWorkflowsWithStageNames() {

        List<WorkflowDef> defs = workflowDefRepository.findAll();
        if (defs.isEmpty()) return List.of();


        List<Long> ids = defs.stream().map(WorkflowDef::getId).toList();
        var rows = stageRepository.findStageNamesByWorkflowIds(ids);


        Map<Long, List<String>> byWorkflowId = rows.stream()
                .collect(Collectors.groupingBy(
                        WorkflowStageRepository.StageNameByWorkflow::getWorkflowId,
                        LinkedHashMap::new,
                        Collectors.mapping(WorkflowStageRepository.StageNameByWorkflow::getName, Collectors.toList())
                ));


        return defs.stream()
                .sorted(Comparator.comparing(WorkflowDef::getId))
                .map(d -> new WorkflowSummaryDto(
                        d.getId(),
                        d.getName(),
                        d.getVersion(),
                        d.getActive(),
                        byWorkflowId.getOrDefault(d.getId(), List.of())
                ))
                .toList();
    }
}
