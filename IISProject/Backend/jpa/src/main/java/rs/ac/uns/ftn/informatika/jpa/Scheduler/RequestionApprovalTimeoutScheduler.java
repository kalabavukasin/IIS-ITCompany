package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Repository.RequestionRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class RequestionApprovalTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(RequestionApprovalTimeoutScheduler.class);
    // Fallback: used only when a requestion has no reviewDeadline set
    private static final int FALLBACK_TIMEOUT_DAYS = 30;

    private final RequestionRepository requestionRepository;

    public RequestionApprovalTimeoutScheduler(RequestionRepository requestionRepository) {
        this.requestionRepository = requestionRepository;
    }

    // Requestions in PENDING_APPROVAL status are moved back to DRAFT when:
    //   - reviewDeadline is set and has passed, OR
    //   - reviewDeadline is not set and they have been pending for more than FALLBACK_TIMEOUT_DAYS days

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    @Transactional
    public void timeoutPendingRequestions() {
        LocalDate today = LocalDate.now();
        OffsetDateTime fallbackThreshold = OffsetDateTime.now().minusDays(FALLBACK_TIMEOUT_DAYS);

        List<Requestion> timedOutRequestions = requestionRepository
                .findPendingRequestionsPastThreshold(today, fallbackThreshold);

        if (timedOutRequestions.isEmpty()) {
            log.debug("No timed-out requestions found");
            return;
        }

        log.info("Found {} requestion(s) pending approval past their deadline", timedOutRequestions.size());

        for (Requestion requestion : timedOutRequestions) {
            try {
                requestion.setStatus(RequestionStatus.DRAFT);

                String timeoutComment = requestion.getReviewDeadline() != null
                        ? "Moved back to DRAFT: review deadline " + requestion.getReviewDeadline() + " passed without approval."
                        : "Moved back to DRAFT after " + FALLBACK_TIMEOUT_DAYS + " days without approval. Please review and resubmit.";

                if (requestion.getHiringComment() != null && !requestion.getHiringComment().isEmpty()) {
                    requestion.setHiringComment(requestion.getHiringComment() + " | " + timeoutComment);
                } else {
                    requestion.setHiringComment(timeoutComment);
                }

                requestionRepository.save(requestion);

                log.info("Requestion ID {} moved back to DRAFT (reviewDeadline={})",
                        requestion.getId(), requestion.getReviewDeadline());

            } catch (Exception e) {
                log.error("Error processing timed-out requestion ID {}: {}",
                        requestion.getId(), e.getMessage(), e);
            }
        }

        log.info("Processed {} timed-out requestion(s)", timedOutRequestions.size());
    }
}
