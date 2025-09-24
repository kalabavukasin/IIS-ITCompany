package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobPosting_IdAndCandidate_Id(Long postingId, Long candidateId);
}
