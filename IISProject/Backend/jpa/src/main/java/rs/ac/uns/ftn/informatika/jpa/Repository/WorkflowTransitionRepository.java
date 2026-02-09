package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowTransition;

import java.util.List;
import java.util.Optional;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    @Query("""
        select t
        from WorkflowTransition t
        where t.fromStage.id = :fromStageId
        order by t.id asc
    """)
    List<WorkflowTransition> findByFromStageId(Long fromStageId);
    List<WorkflowTransition> findByToStage_Id(Long toStageId);

    @Query("SELECT t FROM WorkflowTransition t " +
            "WHERE t.fromStage.id = :fromId AND t.toStage.id = :toId")
    Optional<WorkflowTransition> findByStages(@Param("fromId") Long fromId,
                                              @Param("toId") Long toId);

    @Query("SELECT DISTINCT t FROM WorkflowTransition t " +
            "JOIN t.allowedRoles r " +
            "WHERE t.fromStage.id = :stageId AND r = :role")
    List<WorkflowTransition> findAvailableTransitions(@Param("stageId") Long stageId,
                                                      @Param("role") String role);
}
