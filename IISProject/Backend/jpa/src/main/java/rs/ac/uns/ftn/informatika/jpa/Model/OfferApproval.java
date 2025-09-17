package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferApprovalDecision;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "offer_approvals")
public class OfferApproval {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id", nullable = false)
    private HiringManager hiringManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private OfferApprovalDecision decision;

    @Lob
    private String comment;

    @Column(nullable = false)
    private OffsetDateTime decidedAt;

    public OfferApproval() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }

    public HiringManager getHiringManager() { return hiringManager; }
    public void setHiringManager(HiringManager hiringManager) { this.hiringManager = hiringManager; }

    public OfferApprovalDecision getDecision() { return decision; }
    public void setDecision(OfferApprovalDecision decision) { this.decision = decision; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public OffsetDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
}
