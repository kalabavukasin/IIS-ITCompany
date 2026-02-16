package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.Dto.OfferCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.OfferCreateDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.Offer;
import rs.ac.uns.ftn.informatika.jpa.Repository.OfferRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OfferService {
    private final OfferRepository offerRepo;
    private final ApplicationService applicationService;
    private final AuditService auditService;

    public OfferService (OfferRepository offerRepo, ApplicationService applicationService, AuditService auditService) {
        this.offerRepo = offerRepo;
        this.applicationService = applicationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OfferCardDTO> recentForCandidate(Long candidateId, int daysBack) {
        var cutoff = OffsetDateTime.now().minusDays(daysBack <= 0 ? 30 : daysBack);
        return offerRepo.findRecentOffersForCandidate(candidateId, cutoff);
    }
    @Transactional
    public OfferCardDTO acceptOffer(Long offerId, Long userId) {
        // Postavi user_id za audit log
        auditService.setCurrentUserForAudit(userId);
        
        var o = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        var app = o.getApplication();
        var candidate = app.getCandidate();
        if (candidate == null || !candidate.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer does not belong to this user");
        }

        if (o.getStatus() != OfferStatus.SENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only SENT offers can be accepted");
        }

        applicationService.updateApplicationStatus(o.getApplication().getId(), ApplicationStatus.HIRED);
        o.setStatus(OfferStatus.ACCEPTED);
        offerRepo.save(o);

        var r = app.getJobPosting().getRequestion();
        return new OfferCardDTO(
                o.getId(),
                o.getStatus().name(),
                o.getStartDate(),
                app.getId(),
                r.getName(),
                r.getDescription()
        );
    }
    @Transactional
    public OfferCardDTO declineOffer(Long offerId, Long userId) {
        // Postavi user_id za audit log
        auditService.setCurrentUserForAudit(userId);
        
        var o = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        var app = o.getApplication();
        var candidate = app.getCandidate(); // prilagodi ako je drugačije: app.getCandidateProfile().getUser()...
        if (candidate == null || !candidate.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer does not belong to this user");
        }

        if (o.getStatus() != OfferStatus.SENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only SENT offers can be declined");
        }

        applicationService.updateApplicationStatus(o.getApplication().getId(), ApplicationStatus.REFUSED_OFFER);
        o.setStatus(OfferStatus.DECLINED);
        offerRepo.save(o);

        var r = app.getJobPosting().getRequestion();
        return new OfferCardDTO(
                o.getId(),
                o.getStatus().name(),
                o.getStartDate(),    // LocalDate
                app.getId(),
                r.getName(),
                r.getDescription()
        );
    }

    // Reporting method
    @Transactional(readOnly = true)
    public List<Offer> findOffersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return offerRepo.findOffersByDateRange(startDate, endDate);
    }

    @Transactional
    public void createOffer(OfferCreateDTO dto, Long triggeredById) {
        Application app = applicationService.getApplicationById(dto.applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        Offer offer = new Offer();
        offer.setApplication(app);
        offer.setStartDate(dto.startDate);
        offer.setCreatedAt(OffsetDateTime.now());
        offer.setStatus(OfferStatus.SENT);
        offerRepo.save(offer);

        applicationService.advanceWorkflowOnOfferMade(dto.applicationId, triggeredById);
    }
}
