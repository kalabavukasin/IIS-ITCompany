package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Dto.CreateEvaluationRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.EvaluationDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.EvaluationResponseDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.Evaluation;
import rs.ac.uns.ftn.informatika.jpa.Model.Interview;
import rs.ac.uns.ftn.informatika.jpa.Repository.EvaluationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.InterviewRepository;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Repository.UserRepository;

@Service
public class EvaluationService {
    private final EvaluationRepository evalRepo;
    private final InterviewRepository interviewRepo;
    private final UserRepository userRepo;

    public EvaluationService(EvaluationRepository evalRepo, InterviewRepository interviewRepo,
                             UserRepository userRepo) {
        this.evalRepo = evalRepo;
        this.interviewRepo = interviewRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public EvaluationResponseDTO create(CreateEvaluationRequestDTO req) {
        Interview interview = interviewRepo.findById(req.interviewId())
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + req.interviewId()));
        var interviewer = userRepo.findById(req.interviewerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.interviewerId()));


        evalRepo.findByInterviewIdAndInterviewerId(interview.getId(), interviewer.getId())
                .ifPresent(e -> { throw new IllegalStateException("Evaluation already exists."); });

        var e = new Evaluation();
        e.setInterview(interview);
        e.setInterviewer(interviewer);
        e.setFinalGrade(req.grade());
        e.setComment(req.comment());
        e.setCreatedAt(java.time.OffsetDateTime.now());

        e = evalRepo.save(e);
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepo.save(interview);

        return new EvaluationResponseDTO(
                e.getId(),
                interview.getId(),
                interviewer.getId(),
                e.getFinalGrade(),
                e.getComment(),
                e.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public EvaluationResponseDTO getForInterviewAndInterviewer(Long interviewId, Long interviewerId) {
        var e = evalRepo.findByInterviewIdAndInterviewerId(interviewId, interviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found."));
        return new EvaluationResponseDTO(
                e.getId(),
                e.getInterview().getId(),
                e.getInterviewer().getId(),
                e.getFinalGrade(),
                e.getComment(),
                e.getCreatedAt()
        );
    }
    @Transactional(readOnly = true)
    public EvaluationDetailsDTO getLatestForApplication(Long applicationId) {
        return evalRepo.findLatestByApplication(applicationId).orElse(null);
    }
}
