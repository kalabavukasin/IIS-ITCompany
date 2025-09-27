package rs.ac.uns.ftn.informatika.jpa.Service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewToShowDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewParticipantRole;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewType;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestInviteStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.*;
import rs.ac.uns.ftn.informatika.jpa.Repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final ApplicationService applicationService;
    private final UserRepository userRepository;
    private final TestInviteRepository testRepository;
    private final TestResultRepository testResultRepository;

    public InterviewService(InterviewRepository interviewRepository,
                            InterviewParticipantRepository participantRepository,
                            ApplicationService applicationService,
                            ApplicationRepository applicationRepository,
                            UserRepository userRepository,
                            TestInviteRepository testRepository,
                            TestResultRepository testResultRepository) {
        this.interviewRepository = interviewRepository;
        this.participantRepository = participantRepository;
        this.applicationService = applicationService;
        this.userRepository = userRepository;
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
    }
    @Transactional
    public Interview scheduleInterview(InterviewScheduleDTO dto, Long triggeredByUserId) {

        Application application = applicationService.getApplicationById(dto.applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + dto.applicationId));

        //If we have a test score (coming from Test phase), update the test
        if (dto.testScore != null) {
            TestInvite test = testRepository.findTopByApplicationId(dto.applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found for application"));
            test.setStatus(TestInviteStatus.VERIFIED);
            TestResult testResult = new TestResult();
            testResult.setTestInvite(test);
            testResult.setScore(dto.testScore);
            testResult.setPassed(true);
            testRepository.save(test);
            testResultRepository.save(testResult);
        }

        //Interview initialization
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setType(InterviewType.valueOf(dto.interviewType));
        interview.setScheduledAt(dto.scheduledAt);
        interview.setDurationMinutes(dto.durationMinutes);
        interview.setLocationOrLink(dto.location);
        interview.setStatus(InterviewStatus.SCHEDULED);

        Interview savedInterview = interviewRepository.save(interview);

        // List of participants
        List<InterviewParticipant> participants = new ArrayList<>();

        /* candidateParticipant = new InterviewParticipant();
        candidateParticipant.setInterview(savedInterview);
        candidateParticipant.setUser(application.getCandidate().getUser());
        candidateParticipant.setRoleOnInterview(InterviewParticipantRole.CANDIDATE);
        participants.add(candidateParticipant);*/

        // adding the interviewer
        if (dto.interviewerId != null) {
            User interviewer = userRepository.findById(dto.interviewerId)
                    .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"));

            InterviewParticipant interviewerParticipant = new InterviewParticipant();
            interviewerParticipant.setInterview(savedInterview);
            interviewerParticipant.setUser(interviewer);
            interviewerParticipant.setRoleOnInterview(InterviewParticipantRole.INTERVIEWER);
            participants.add(interviewerParticipant);
        }

        //adding observers
        if (dto.observerIds != null && !dto.observerIds.isEmpty()) {
            for (Long observerId : dto.observerIds) {
                User observer = userRepository.findById(observerId)
                        .orElseThrow(() -> new IllegalArgumentException("Observer not found: " + observerId));

                InterviewParticipant observerParticipant = new InterviewParticipant();
                observerParticipant.setInterview(savedInterview);
                observerParticipant.setUser(observer);
                observerParticipant.setRoleOnInterview(InterviewParticipantRole.OBSERVER);
                participants.add(observerParticipant);
            }
        }

        participantRepository.saveAll(participants);

        // Advance workflow to Interview phase
        applicationService.advanceWorkflowOnInterviewScheduled(dto.applicationId, triggeredByUserId);

        return savedInterview;
    }

    public List<Interview> getInterviewsByApplication(Long applicationId) {
        return interviewRepository.findByApplication_Id(applicationId);
    }

    public List<InterviewParticipant> getParticipantsByInterview(Long interviewId) {
        return participantRepository.findByInterview_Id(interviewId);
    }
    public Optional<InterviewDetailsDTO> getDetailsByApplicationId(Long applicationId) {

        return interviewRepository.findByApplicationId(applicationId)
                .map(this::mapToDto);
    }

    private InterviewDetailsDTO mapToDto(Interview inv) {

        return new InterviewDetailsDTO(
                inv.getId(),
                inv.getType(),
                inv.getScheduledAt(),
                inv.getDurationMinutes(),
                inv.getLocationOrLink(),
                inv.getStatus()
        );
    }
    @Transactional(readOnly = true)
    public List<InterviewToShowDTO> getInterviewsByInterviewerId(Long interviewerId) {
        return participantRepository.findInterviewsToShowByInterviewerId(interviewerId);
    }
    @Transactional(readOnly = true)
    public List<InterviewToShowDTO> getObservedInterviewsByUserId(Long userId) {
        return participantRepository.findInterviewsToShowByUserAndRole(userId,InterviewParticipantRole.OBSERVER);
    }
}
