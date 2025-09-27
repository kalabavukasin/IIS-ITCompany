package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Dto.EvaluationDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Evaluation;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByInterviewIdAndInterviewerId(Long interviewId, Long interviewerId);

    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.EvaluationDetailsDTO(
            e.id, e.finalGrade, e.comment, e.createdAt
        )
        from Evaluation e
          join e.interview i
        where i.application.id = :applicationId
        order by e.createdAt desc
    """)
    Optional<EvaluationDetailsDTO> findLatestByApplication(@Param("applicationId") Long applicationId);

}
