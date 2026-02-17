package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Repository.RequestionRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class RequestionApprovalTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(RequestionApprovalTimeoutScheduler.class);
    private static final int APPROVAL_TIMEOUT_DAYS = 30;

    private final RequestionRepository requestionRepository;

    public RequestionApprovalTimeoutScheduler(RequestionRepository requestionRepository) {
        this.requestionRepository = requestionRepository;
    }

    // Requestions in PENDING_APPROVAL status for more than 30 days are moved back to DRAFT
    // this gives hiring managers a chance to review and resubmit

    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    @Transactional
    public void timeoutPendingRequestions() {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(APPROVAL_TIMEOUT_DAYS);

        // Find all PENDING_APPROVAL requestions older than 30 days
        List<Requestion> timedOutRequestions = requestionRepository.findPendingRequestionsPastThreshold(threshold);

        if (timedOutRequestions.isEmpty()) {
            log.debug("No timed-out requestions found");
            return;
        }

        log.info("Found {} requestion(s) pending approval for more than {} days",
            timedOutRequestions.size(), APPROVAL_TIMEOUT_DAYS);

        for (Requestion requestion : timedOutRequestions) {
            try {
                // Move back to DRAFT status
                requestion.setStatus(RequestionStatus.DRAFT);

                // Add comment explaining why it was moved back to draft
                String timeoutComment = "Moved back to DRAFT after " + APPROVAL_TIMEOUT_DAYS +
                    " days without approval. Please review and resubmit.";

                // Append to existing comment if any
                if (requestion.getHiringComment() != null && !requestion.getHiringComment().isEmpty()) {
                    requestion.setHiringComment(requestion.getHiringComment() + " | " + timeoutComment);
                } else {
                    requestion.setHiringComment(timeoutComment);
                }

                requestionRepository.save(requestion);

                log.info("Requestion ID {} moved back to DRAFT after {} days pending approval",
                    requestion.getId(), APPROVAL_TIMEOUT_DAYS);

            } catch (Exception e) {
                log.error("Error processing timed-out requestion ID {}: {}",
                    requestion.getId(), e.getMessage(), e);
            }
        }

        log.info("Processed {} timed-out requestion(s)", timedOutRequestions.size());
    }
}
