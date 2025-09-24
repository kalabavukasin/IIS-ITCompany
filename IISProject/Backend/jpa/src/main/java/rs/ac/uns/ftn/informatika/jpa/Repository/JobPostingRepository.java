package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.JobPosting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Optional<JobPosting> findByRequestion_Id(Long requestionId);

    List<JobPosting> findByStatusAndValidToGreaterThanEqualOrderByCreatedAtDesc(
            JobPostingStatus status, LocalDate today);

    @Query("""
       select p from JobPosting p
       join fetch p.requestion r
       where p.status = rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus.PUBLISHED
         and p.validTo >= :today
       order by p.createdAt desc
    """)
    List<JobPosting> findOpenWithRequestion(LocalDate today);
    @Query("""
      select p from JobPosting p
      join fetch p.requestion r
      where p.id = :id
    """)
    Optional<JobPosting> findByIdWithRequestion(@Param("id") Long id);
}
