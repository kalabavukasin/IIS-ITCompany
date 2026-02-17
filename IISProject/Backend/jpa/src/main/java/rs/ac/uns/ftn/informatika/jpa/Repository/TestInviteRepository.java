package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestInviteStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.TestInvite;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TestInviteRepository extends JpaRepository<TestInvite, Long> {
    Optional<TestInvite> findTopByApplicationId(Long applicationId);
    Optional<TestInvite> findByApplicationId(Long applicationId);

    @Query("""
        SELECT t FROM TestInvite t
        WHERE t.status = :status
        AND t.deadline < :now
    """)
    List<TestInvite> findExpiredTests(@Param("status") TestInviteStatus status,
                                      @Param("now") OffsetDateTime now);
}
