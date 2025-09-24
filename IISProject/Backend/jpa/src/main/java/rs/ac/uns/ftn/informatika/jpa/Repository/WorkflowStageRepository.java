package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowStage;

public interface WorkflowStageRepository extends JpaRepository<WorkflowStage, Long> {
    WorkflowStage findFirstByWorkflow_IdOrderBySortOrderAsc(Long workflowId);
}
