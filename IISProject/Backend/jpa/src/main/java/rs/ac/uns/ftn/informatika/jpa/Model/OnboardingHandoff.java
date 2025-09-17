package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "onboarding_handoff")
public class OnboardingHandoff {

    @Id @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", unique = true)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private CandidateProfile candidate;

    private LocalDate startDate;
    private String status;

    public OnboardingHandoff() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }

    public CandidateProfile getCandidate() { return candidate; }
    public void setCandidate(CandidateProfile candidate) { this.candidate = candidate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

