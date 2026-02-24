package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RequestionRepository extends JpaRepository<Requestion, Long> {

    List<Requestion> findByCreatedBy_IdOrderByCreatedAtDesc(Long creatorId);
    List<Requestion> findByHiringManager_IdAndStatusInOrderByCreatedAtDesc(Long hmId, List<RequestionStatus> statuses);

    @Query("""
        SELECT r FROM Requestion r
        WHERE r.status = rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus.PENDING_APPROVAL
        AND (
            (r.reviewDeadline IS NOT NULL AND r.reviewDeadline < :today)
            OR
            (r.reviewDeadline IS NULL AND r.createdAt < :fallbackThreshold)
        )
    """)
    List<Requestion> findPendingRequestionsPastThreshold(
            @Param("today") LocalDate today,
            @Param("fallbackThreshold") OffsetDateTime fallbackThreshold);
}
