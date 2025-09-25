package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowStage;

import java.util.List;
import java.util.Optional;

public interface WorkflowStageRepository extends JpaRepository<WorkflowStage, Long> {
    WorkflowStage findFirstByWorkflow_IdOrderBySortOrderAsc(Long workflowId);
    @Query("""
        select s
        from WorkflowStage s
        where s.workflow.id = :defId
        order by s.sortOrder asc, s.id asc
    """)
    List<WorkflowStage> findAllOrderedByDefId(Long defId);

    // Po imenu faze u okviru definicije
    @Query("""
        select s
        from WorkflowStage s
        where s.workflow.id = :defId and s.name = :name
    """)
    Optional<WorkflowStage> findByDefIdAndName(Long defId, String name);
}
