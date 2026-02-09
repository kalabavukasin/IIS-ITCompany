package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Interview;
import rs.ac.uns.ftn.informatika.jpa.Model.TestInvite;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplication_Id(Long applicationId);

    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByScheduledAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT i FROM Interview i " +
            "JOIN FETCH i.application a " +
            "JOIN FETCH a.candidate " +
            "WHERE i.id = :id")
    Optional<Interview> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT i FROM Interview i " +
            "WHERE i.scheduledAt > :now " +
            "AND i.status = 'SCHEDULED' " +
            "ORDER BY i.scheduledAt ASC")
    List<Interview> findUpcomingInterviews(@Param("now") OffsetDateTime now);
    Optional<Interview> findByApplicationId(Long applicationId);
}
