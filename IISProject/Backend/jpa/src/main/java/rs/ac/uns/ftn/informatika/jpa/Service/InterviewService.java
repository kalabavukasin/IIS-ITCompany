package rs.ac.uns.ftn.informatika.jpa.Service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Dto.BulkInterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.BulkTestScoreEntryDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewToShowDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewParticipantRole;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewType;
import rs.ac.uns.ftn.informatika.jpa.Model.*;
import rs.ac.uns.ftn.informatika.jpa.Repository.InterviewRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.InterviewParticipantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final ApplicationService applicationService;
    private final UserService userService;
    private final TestService testService;
    private final EmailService emailService;

    public InterviewService(InterviewRepository interviewRepository,
                            InterviewParticipantRepository participantRepository,
                            ApplicationService applicationService,
                            UserService userService,
                            TestService testService,
                            EmailService emailService) {
        this.interviewRepository = interviewRepository;
        this.participantRepository = participantRepository;
        this.applicationService = applicationService;
        this.userService = userService;
        this.testService = testService;
        this.emailService = emailService;
    }
    @Transactional
    public Interview scheduleInterview(InterviewScheduleDTO dto, Long triggeredByUserId) {

        Application application = applicationService.getApplicationById(dto.applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + dto.applicationId));

        //If we have a test score (coming from Test phase), update the test
        if (dto.testScore != null) {
            testService.verifyTestWithScore(dto.applicationId, dto.testScore);
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

        List<InterviewParticipant> participants = new ArrayList<>();

        /* candidateParticipant = new InterviewParticipant();
        candidateParticipant.setInterview(savedInterview);
        candidateParticipant.setUser(application.getCandidate().getUser());
        candidateParticipant.setRoleOnInterview(InterviewParticipantRole.CANDIDATE);
        participants.add(candidateParticipant);*/

        if (dto.interviewerId != null) {
            User interviewer = userService.getUserById(dto.interviewerId)
                    .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"));

            InterviewParticipant interviewerParticipant = new InterviewParticipant();
            interviewerParticipant.setInterview(savedInterview);
            interviewerParticipant.setUser(interviewer);
            interviewerParticipant.setRoleOnInterview(InterviewParticipantRole.INTERVIEWER);
            participants.add(interviewerParticipant);
        }

        if (dto.observerIds != null && !dto.observerIds.isEmpty()) {
            for (Long observerId : dto.observerIds) {
                User observer = userService.getUserById(observerId)
                        .orElseThrow(() -> new IllegalArgumentException("Observer not found: " + observerId));

                InterviewParticipant observerParticipant = new InterviewParticipant();
                observerParticipant.setInterview(savedInterview);
                observerParticipant.setUser(observer);
                observerParticipant.setRoleOnInterview(InterviewParticipantRole.OBSERVER);
                participants.add(observerParticipant);
            }
        }

        participantRepository.saveAll(participants);

        applicationService.advanceWorkflowOnInterviewScheduled(dto.applicationId, triggeredByUserId);

        emailService.sendInterviewScheduledNotification(
                application.getCandidate().getEmail(),
                application.getCandidate().getFirstName(),
                application.getJobPosting().getRequestion().getName(),
                dto.interviewType,
                dto.scheduledAt.toString(),
                dto.location);

        return savedInterview;
    }

    @Transactional
    public void bulkSchedule(BulkInterviewScheduleDTO dto, Long triggeredByUserId) {
        String batchId = UUID.randomUUID().toString();
        int breakMin = (dto.breakMinutes != null) ? dto.breakMinutes : 15;
        int gap = dto.durationMinutes + breakMin;

        User interviewer = dto.interviewerId != null
                ? userService.getUserById(dto.interviewerId)
                    .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"))
                : null;

        List<User> observers = new ArrayList<>();
        if (dto.observerIds != null) {
            for (Long obsId : dto.observerIds) {
                observers.add(userService.getUserById(obsId)
                        .orElseThrow(() -> new IllegalArgumentException("Observer not found: " + obsId)));
            }
        }

        for (int i = 0; i < dto.applicationIds.size(); i++) {
            Long appId = dto.applicationIds.get(i);
            Application application = applicationService.getApplicationById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

            if (dto.testScores != null) {
                dto.testScores.stream()
                        .filter(e -> e.applicationId.equals(appId))
                        .findFirst()
                        .ifPresent(e -> testService.verifyTestWithScore(appId, e.score));
            }

            Interview interview = new Interview();
            interview.setApplication(application);
            interview.setType(InterviewType.valueOf(dto.interviewType));
            interview.setScheduledAt(dto.firstScheduledAt.plusMinutes((long) i * gap));
            interview.setDurationMinutes(dto.durationMinutes);
            interview.setLocationOrLink(dto.location);
            interview.setStatus(InterviewStatus.SCHEDULED);
            interview.setBatchId(batchId);
            Interview saved = interviewRepository.save(interview);

            List<InterviewParticipant> participants = new ArrayList<>();
            if (interviewer != null) {
                InterviewParticipant ip = new InterviewParticipant();
                ip.setInterview(saved);
                ip.setUser(interviewer);
                ip.setRoleOnInterview(InterviewParticipantRole.INTERVIEWER);
                participants.add(ip);
            }
            for (User obs : observers) {
                InterviewParticipant ip = new InterviewParticipant();
                ip.setInterview(saved);
                ip.setUser(obs);
                ip.setRoleOnInterview(InterviewParticipantRole.OBSERVER);
                participants.add(ip);
            }
            participantRepository.saveAll(participants);
            applicationService.advanceWorkflowOnInterviewScheduled(appId, triggeredByUserId);

            emailService.sendInterviewScheduledNotification(
                    application.getCandidate().getEmail(),
                    application.getCandidate().getFirstName(),
                    application.getJobPosting().getRequestion().getName(),
                    dto.interviewType,
                    interview.getScheduledAt().toString(),
                    dto.location);
        }
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
