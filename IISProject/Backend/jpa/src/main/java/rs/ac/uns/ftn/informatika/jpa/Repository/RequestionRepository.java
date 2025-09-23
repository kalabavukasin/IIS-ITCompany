package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;

import java.util.List;
import java.util.Optional;

public interface RequestionRepository extends JpaRepository<Requestion, Long> {

    List<Requestion> findByCreatedBy_IdOrderByCreatedAtDesc(Long creatorId);
    List<Requestion> findByHiringManager_IdAndStatusInOrderByCreatedAtDesc(Long hmId, List<RequestionStatus> statuses);
}
