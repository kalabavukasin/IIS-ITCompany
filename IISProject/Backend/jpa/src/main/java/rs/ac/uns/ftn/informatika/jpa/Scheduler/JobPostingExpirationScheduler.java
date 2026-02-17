package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.JobPosting;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class JobPostingExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobPostingExpirationScheduler.class);

    private final JobPostingRepository jobPostingRepository;

    public JobPostingExpirationScheduler(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

     // Job postings with status PUBLISHED and validTo date in the past are marked as ARCHIVED
     // applications are not affected by this process

    @Scheduled(cron = "0 0 1 * * *") // Every day at 1:00 AM
    @Transactional
    public void archiveExpiredJobPostings() {
        LocalDate today = LocalDate.now();

        // Find all PUBLISHED job postings where validTo is in the past
        List<JobPosting> expiredPostings = jobPostingRepository.findExpiredJobPostings(today);

        if (expiredPostings.isEmpty()) {
            log.debug("No expired job postings found");
            return;
        }

        log.info("Found {} expired job posting(s) to archive", expiredPostings.size());

        for (JobPosting posting : expiredPostings) {
            try {
                // Mark job posting as ARCHIVED
                posting.setStatus(JobPostingStatus.ARCHIVED);
                jobPostingRepository.save(posting);

                log.info("Job posting ID {} archived (validTo: {})",
                    posting.getId(), posting.getValidTo());

            } catch (Exception e) {
                log.error("Error archiving job posting ID {}: {}",
                    posting.getId(), e.getMessage(), e);
            }
        }

        log.info("Archived {} expired job posting(s)", expiredPostings.size());
    }
}
