package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.Model.CandidateProfile;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile,Long> {
}
