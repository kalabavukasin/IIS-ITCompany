package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferApprovalDecision;

import java.time.OffsetDateTime;

@Entity
@Table(name = "offer_approvals")
public class OfferApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id", nullable = false)
    private User hiringManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private OfferApprovalDecision decision;

    @Lob
    private String comment;

    @Column(nullable = false)
    private OffsetDateTime decidedAt;

    public OfferApproval() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }

    public User getHiringManager() { return hiringManager; }
    public void setHiringManager(User hiringManager) { this.hiringManager = hiringManager; }

    public OfferApprovalDecision getDecision() { return decision; }
    public void setDecision(OfferApprovalDecision decision) { this.decision = decision; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public OffsetDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
}
