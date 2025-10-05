package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.Dto.OfferCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Offer;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.OfferCardDTO(
            o.id,
            cast(o.status as string),
            o.startDate,
            a.id,
            r.name,
            r.description
        )
        from Offer o
          join o.application a
          join a.jobPosting jp
          join jp.requestion r
        where a.candidate.id = :candidateId
          and o.createdAt >= :cutoff
        order by o.createdAt desc
    """)
    List<OfferCardDTO> findRecentOffersForCandidate(@Param("candidateId") Long candidateId,
                                                    @Param("cutoff") OffsetDateTime cutoff);

    @Query("""
        SELECT o FROM Offer o 
        WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate
    """)
    List<rs.ac.uns.ftn.informatika.jpa.Model.Offer> findOffersByDateRange(@Param("startDate") OffsetDateTime startDate, 
                                                                         @Param("endDate") OffsetDateTime endDate);
}
