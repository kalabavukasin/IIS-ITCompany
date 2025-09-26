package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.ApplicationStatusHistory;

import java.util.List;
import java.util.Optional;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    Optional<ApplicationStatusHistory> findTopByApplicationAndExitedAtIsNullOrderByEnteredAtDesc(Application application);

    List<ApplicationStatusHistory> findByApplicationOrderByEnteredAtAsc(Application application);

    List<ApplicationStatusHistory> findByApplication_IdOrderByEnteredAtAsc(Long applicationId);

    @Query("SELECT h FROM ApplicationStatusHistory h " +
            "WHERE h.application.id = :appId AND h.exitedAt IS NULL")
    Optional<ApplicationStatusHistory> findCurrentHistory(@Param("appId") Long appId);

    @Query("SELECT COUNT(h) FROM ApplicationStatusHistory h " +
            "WHERE h.application.id = :appId")
    Long countByApplicationId(@Param("appId") Long appId);
}
