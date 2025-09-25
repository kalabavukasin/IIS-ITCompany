package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobPosting_IdAndCandidate_Id(Long postingId, Long candidateId);
    List<Application> findByCandidate_IdOrderByAppliedAtDesc(Long candidateId);

    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationCardDTO(
            a.id,
            a.status,
            jp.id,
            rq.name,
            rq.description
        )
        from Application a
            join a.jobPosting jp
            join jp.requestion rq
        where a.candidate.id = :candidateId
        order by a.appliedAt desc
    """)
    List<ApplicationCardDTO> findCardsByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationWithUserDTO(
            a.id,
            cast(a.status as string),
            cs.name,
            jp.id,
            r.name,
            r.description,
            r.location,
            u.id,
            concat(u.firstName, ' ', u.lastName),
            jp.validTo
        )
        from Application a
        join a.candidate u
        join a.jobPosting jp
        join jp.requestion r
        left join a.currentStage cs
        order by a.id desc
    """)
    List<rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationWithUserDTO> findAllCards();

    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDetailsDTO(
            a.id,
            cast(a.status as string),
            a.appliedAt,
            jp.id,
            r.name,
            r.description,
            r.location,
            cast(r.seniority as string),
            r.budget,
            r.skills,
            concat(hr.firstName, ' ', hr.lastName),
            jp.validTo,

            u.id,
            concat(u.firstName, ' ', u.lastName),
            u.email,
            u.phone,
            null,
            cs.name,
            null
        )
        from Application a
        join a.jobPosting jp
        join jp.requestion r
        join a.candidate u
        left join a.currentStage cs
        left join r.createdBy hr
        where a.id = :applicationId
    """)
    Optional<ApplicationDetailsDTO> findRawDetails(Long applicationId);

    @Query("""
         select wd.id
        from Application a
        left join a.currentStage cs
        left join cs.workflow wd
         where a.id = :appId
        """)
    java.util.Optional<Long> findWorkflowIdByApplicationId(@Param("appId") Long appId);
}
