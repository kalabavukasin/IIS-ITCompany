package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.Dto.OfferCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.OfferRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OfferService {
    private final OfferRepository offerRepo;
    private final ApplicationRepository applicationRepo;

    public OfferService (OfferRepository offerRepo, ApplicationRepository applicationRepo) {
        this.offerRepo = offerRepo;
        this.applicationRepo = applicationRepo;
    }

    @Transactional(readOnly = true)
    public List<OfferCardDTO> recentForCandidate(Long candidateId, int daysBack) {
        var cutoff = OffsetDateTime.now().minusDays(daysBack <= 0 ? 30 : daysBack);
        return offerRepo.findRecentOffersForCandidate(candidateId, cutoff);
    }
    @Transactional
    public OfferCardDTO acceptOffer(Long offerId, Long userId) {
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
        Application application = applicationRepo.findById(o.getApplication().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        application.setStatus(ApplicationStatus.HIRED);
        applicationRepo.save(application);
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

        Application application = applicationRepo.findById(o.getApplication().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        application.setStatus(ApplicationStatus.REFUSED_OFFER);
        applicationRepo.save(application);
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
}
