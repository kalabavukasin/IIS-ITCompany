package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowTransition;

import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    @Query("""
        select t
        from WorkflowTransition t
        where t.fromStage.id = :fromStageId
        order by t.id asc
    """)
    List<WorkflowTransition> findByFromStageId(Long fromStageId);
}
