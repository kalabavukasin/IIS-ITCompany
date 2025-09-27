package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.Model.TestInvite;

import java.util.Optional;

public interface TestInviteRepository extends JpaRepository<TestInvite, Long> {
    Optional<TestInvite> findTopByApplicationId(Long applicationId);
}
