package rs.ac.uns.ftn.informatika.jpa.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestInviteStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.TestInvite;
import rs.ac.uns.ftn.informatika.jpa.Repository.TestInviteRepository;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class TestInviteExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TestInviteExpirationScheduler.class);

    private final TestInviteRepository testInviteRepository;
    private final ApplicationService applicationService;

    public TestInviteExpirationScheduler(TestInviteRepository testInviteRepository,
                                        ApplicationService applicationService) {
        this.testInviteRepository = testInviteRepository;
        this.applicationService = applicationService;
    }

    // Tests with status SENT and deadline passed are marked as EXPIRED
    // their applications are automatically REJECTED

    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    @Transactional
    public void expireTests() {
        OffsetDateTime now = OffsetDateTime.now();

        // Find all SENT tests where deadline has passed
        List<TestInvite> expiredTests = testInviteRepository.findExpiredTests(
            TestInviteStatus.SENT,
            now
        );

        if (expiredTests.isEmpty()) {
            log.debug("No expired tests found at {}", now);
            return;
        }

        log.info("Found {} expired test(s) to process", expiredTests.size());

        for (TestInvite test : expiredTests) {
            try {
                test.setStatus(TestInviteStatus.EXPIRED);
                testInviteRepository.save(test);

                Long applicationId = test.getApplication().getId();
                applicationService.refuse(applicationId, "Test deadline expired");

                log.info("Test ID {} expired and application ID {} rejected",
                    test.getId(), applicationId);

            } catch (Exception e) {
                log.error("Error processing expired test ID {}: {}",
                    test.getId(), e.getMessage(), e);
            }
        }

        log.info("Processed {} expired test(s)", expiredTests.size());
    }
}
