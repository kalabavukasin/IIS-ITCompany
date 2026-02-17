package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Interview;
import rs.ac.uns.ftn.informatika.jpa.Repository.InterviewRepository;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class InterviewNoShowScheduler {

    private static final Logger log = LoggerFactory.getLogger(InterviewNoShowScheduler.class);
    private static final int GRACE_PERIOD_DAYS = 1;

    private final InterviewRepository interviewRepository;
    private final ApplicationService applicationService;

    public InterviewNoShowScheduler(InterviewRepository interviewRepository,
                                   ApplicationService applicationService) {
        this.interviewRepository = interviewRepository;
        this.applicationService = applicationService;
    }

    // Interviews that are still SCHEDULED more than 1 day after their scheduled time
    // are marked as NO_SHOW and their applications are automatically REJECTED

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3:00 AM
    @Transactional
    public void markNoShowInterviews() {
        OffsetDateTime now = OffsetDateTime.now();

        // Find all SCHEDULED interviews that should have already happened
        OffsetDateTime searchThreshold = now.minusDays(GRACE_PERIOD_DAYS);
        List<Interview> overdueInterviews = interviewRepository.findOverdueScheduledInterviews(searchThreshold);

        if (overdueInterviews.isEmpty()) {
            log.debug("No overdue interviews found");
            return;
        }

        log.info("Found {} potentially overdue interview(s) to check", overdueInterviews.size());

        int noShowCount = 0;
        for (Interview interview : overdueInterviews) {
            try {
                // Calculate when the interview was supposed to end
                OffsetDateTime interviewEndTime = interview.getScheduledAt()
                    .plusMinutes(interview.getDurationMinutes() != null ? interview.getDurationMinutes() : 60);

                // Add grace period of 1 day
                OffsetDateTime noShowThreshold = interviewEndTime.plusDays(GRACE_PERIOD_DAYS);

                // Check if enough time has passed
                if (now.isAfter(noShowThreshold)) {
                    // Mark interview as NO_SHOW
                    interview.setStatus(InterviewStatus.NO_SHOW);
                    interviewRepository.save(interview);

                    // Reject the application with reason
                    Long applicationId = interview.getApplication().getId();
                    applicationService.refuse(applicationId, "Candidate did not show up for interview");

                    log.info("Interview ID {} marked as NO_SHOW and application ID {} rejected (scheduled: {}, duration: {} min)",
                        interview.getId(), applicationId, interview.getScheduledAt(), interview.getDurationMinutes());

                    noShowCount++;
                }

            } catch (Exception e) {
                log.error("Error processing overdue interview ID {}: {}",
                    interview.getId(), e.getMessage(), e);
            }
        }

        if (noShowCount > 0) {
            log.info("Marked {} interview(s) as NO_SHOW", noShowCount);
        } else {
            log.debug("No interviews met the NO_SHOW criteria");
        }
    }
}
