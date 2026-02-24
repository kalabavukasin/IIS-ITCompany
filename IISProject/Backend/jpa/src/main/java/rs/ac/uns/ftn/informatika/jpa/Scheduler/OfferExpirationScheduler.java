package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.OfferStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Offer;
import rs.ac.uns.ftn.informatika.jpa.Repository.OfferRepository;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;

import java.time.LocalDate;
import java.util.List;

@Component
public class OfferExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OfferExpirationScheduler.class);

    private final OfferRepository offerRepository;
    private final ApplicationService applicationService;

    public OfferExpirationScheduler(OfferRepository offerRepository,
                                   ApplicationService applicationService) {
        this.offerRepository = offerRepository;
        this.applicationService = applicationService;
    }

    // Offers with status SENT whose validUntil date has passed are marked as EXPIRED
    // their applications are automatically marked as REFUSED_OFFER

    @Scheduled(cron = "0 0 4 * * *") // Every day at 4:00 AM
    @Transactional
    public void expireOffers() {
        // Find all SENT offers whose validUntil date is in the past
        List<Offer> expiredOffers = offerRepository.findExpiredOffers(LocalDate.now());

        if (expiredOffers.isEmpty()) {
            log.debug("No expired offers found");
            return;
        }

        log.info("Found {} expired offer(s) to process", expiredOffers.size());

        for (Offer offer : expiredOffers) {
            try {
                // Mark offer as EXPIRED
                offer.setStatus(OfferStatus.EXPIRED);
                offerRepository.save(offer);

                // Update application status to REFUSED_OFFER with note
                Long applicationId = offer.getApplication().getId();
                applicationService.updateApplicationStatusWithNote(
                    applicationId,
                    ApplicationStatus.REFUSED_OFFER,
                    "Offer expired (valid until " + offer.getValidUntil() + ")"
                );

                log.info("Offer ID {} expired and application ID {} marked as REFUSED_OFFER",
                    offer.getId(), applicationId);

            } catch (Exception e) {
                log.error("Error processing expired offer ID {}: {}",
                    offer.getId(), e.getMessage(), e);
            }
        }

        log.info("Processed {} expired offer(s)", expiredOffers.size());
    }
}
