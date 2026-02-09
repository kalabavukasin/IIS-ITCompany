package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rs.ac.uns.ftn.informatika.jpa.Dto.SavedTestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestInviteRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestRefuseDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestInviteStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.TestInvite;
import rs.ac.uns.ftn.informatika.jpa.Model.TestResult;
import rs.ac.uns.ftn.informatika.jpa.Repository.TestInviteRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.TestResultRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class TestService {

    private final TestStorageService storage;
    private final TestInviteRepository testInviteRepository;
    private final ApplicationService applicationService;
    private final TestResultRepository testResultRepository;

    public TestService(TestStorageService storage, TestInviteRepository testInviteRepository,
                       ApplicationService applicationService,
                       TestResultRepository testResultRepository) {
        this.storage = storage;
        this.testInviteRepository = testInviteRepository;
        this.applicationService = applicationService;
        this.testResultRepository = testResultRepository;
    }
    @Transactional
    public SavedTestDTO createInvite(TestInviteRequestDTO req, MultipartFile file) throws IOException {
        if (req == null) throw new IllegalArgumentException("Meta is required.");
        if (req.applicationId == null) throw new IllegalArgumentException("applicationId is required.");
        if (req.type == null) throw new IllegalArgumentException("test type is required.");
        if (req.activeUntil == null) throw new IllegalArgumentException("activeUntil is required.");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is required.");


        OffsetDateTime now = OffsetDateTime.now();
        if (req.activeUntil.isBefore(now)) {
            throw new IllegalArgumentException("activeUntil must be in the future.");
        }
        SavedTestDTO saved = storage.save(file);
        Application app = applicationService.getApplicationById(req.applicationId).orElseThrow(() -> new IllegalStateException(
                "WorkflowDef not found or inactive for id=" + req.applicationId));

        TestInvite invite = new TestInvite();
        invite.setApplication(app);
        invite.setTestType(req.type);
        invite.setDeadline(req.activeUntil);
        invite.setStatus(TestInviteStatus.SENT);
        invite.setTestUrl(saved.publicUrl);

        testInviteRepository.save(invite);
        applicationService.advanceWorkflowOnTestSent(app.getId(), req.triggeredById);
        return saved;
    }
    @Transactional
    public SavedTestDTO updateTest(Long id, MultipartFile file) throws IOException {

        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String type = Optional.ofNullable(file.getContentType()).orElse("");
        boolean okType = type.matches(".*(pdf|msword|officedocument|zip|rar|7z).*")
                || name.toLowerCase().matches(".*\\.(pdf|doc|docx|odt|zip|rar|7z)$");
        boolean okSize = file.getSize() <= 10 * 1024 * 1024;

        if (!okType) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type.");
        if (!okSize) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large (max 10MB).");

        TestInvite testInvite = testInviteRepository.findById(id).orElseThrow(() -> new IllegalStateException());

        SavedTestDTO saved = storage.save(file);

        testInvite.setTestUrl(saved.publicUrl);
        testInvite.setStatus(TestInviteStatus.COMPLETED);

        testInviteRepository.save(testInvite);
        return saved;
    }
    @Transactional
    public void refuseWithScore(Long applicationId, TestRefuseDTO dto) {

        TestInvite test = testInviteRepository
                .findTopByApplicationId(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found for application"));

        test.setStatus(TestInviteStatus.FAILED);
        TestResult testResult = new TestResult();
        testResult.setTestInvite(test);
        testResult.setScore(dto.score);
        testResult.setPassed(false);
        testInviteRepository.save(test);
        testResultRepository.save(testResult);
        applicationService.refuse(applicationId,dto.reason);
    }
    public Optional<TestDetailsDTO> getDetailsByApplicationId(Long applicationId) {

        return testInviteRepository.findByApplicationId(applicationId)
                .map(this::mapToDto);
    }

    private TestDetailsDTO mapToDto(TestInvite inv) {

        var resultOpt = testResultRepository.findByTestInviteId(inv.getId());
        Boolean passed = resultOpt.map(TestResult::getPassed).orElse(null);
        BigDecimal score = resultOpt.map(TestResult::getScore).orElse(null);


        String testUrl = null;
        if(inv.getTestUrl() != null) {
            testUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/tests/")
                    .path(inv.getTestUrl())
                    .toUriString();
        }
        return new TestDetailsDTO(
                inv.getId(),
                inv.getTestType(),
                inv.getStatus(),
                inv.getDeadline(),
                testUrl,
                passed,
                score
        );
    }
}
