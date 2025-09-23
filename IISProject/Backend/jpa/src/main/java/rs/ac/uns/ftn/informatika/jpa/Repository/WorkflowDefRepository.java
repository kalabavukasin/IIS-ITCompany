package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowDef;

import java.util.Optional;

public interface WorkflowDefRepository extends JpaRepository<WorkflowDef, Long> {
    Optional<WorkflowDef> findByNameAndActiveIsTrue(String name);
}
