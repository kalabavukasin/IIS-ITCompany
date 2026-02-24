package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uq_application_candidate_job",
                columnNames = {"candidate_id", "job_posting_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateProfile candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDef workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id", nullable = false)
    private WorkflowStage currentStage;

    @Column(nullable = false)
    private OffsetDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "application_status")
    private ApplicationStatus status;

    private String note;

    @Column(name = "auto_ai_score")
    private Integer autoAiScore;

    @Column(name = "auto_ai_score_note", columnDefinition = "TEXT")
    private String autoAiScoreNote;

    @Column(name = "auto_ai_scored_at")
    private OffsetDateTime autoAiScoredAt;

    @Column(name = "bulk_ai_score")
    private Integer bulkAiScore;

    @Column(name = "bulk_ai_score_note", columnDefinition = "TEXT")
    private String bulkAiScoreNote;

    @Column(name = "bulk_ai_scored_at")
    private OffsetDateTime bulkAiScoredAt;

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting jobPosting) { this.jobPosting = jobPosting; }

    public CandidateProfile getCandidate() { return candidate; }
    public void setCandidate(CandidateProfile candidate) { this.candidate = candidate; }

    public WorkflowDef getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDef workflow) { this.workflow = workflow; }

    public WorkflowStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(WorkflowStage currentStage) { this.currentStage = currentStage; }

    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(OffsetDateTime appliedAt) { this.appliedAt = appliedAt; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getAutoAiScore() { return autoAiScore; }
    public void setAutoAiScore(Integer autoAiScore) { this.autoAiScore = autoAiScore; }

    public String getAutoAiScoreNote() { return autoAiScoreNote; }
    public void setAutoAiScoreNote(String autoAiScoreNote) { this.autoAiScoreNote = autoAiScoreNote; }

    public OffsetDateTime getAutoAiScoredAt() { return autoAiScoredAt; }
    public void setAutoAiScoredAt(OffsetDateTime autoAiScoredAt) { this.autoAiScoredAt = autoAiScoredAt; }

    public Integer getBulkAiScore() { return bulkAiScore; }
    public void setBulkAiScore(Integer bulkAiScore) { this.bulkAiScore = bulkAiScore; }

    public String getBulkAiScoreNote() { return bulkAiScoreNote; }
    public void setBulkAiScoreNote(String bulkAiScoreNote) { this.bulkAiScoreNote = bulkAiScoreNote; }

    public OffsetDateTime getBulkAiScoredAt() { return bulkAiScoredAt; }
    public void setBulkAiScoredAt(OffsetDateTime bulkAiScoredAt) { this.bulkAiScoredAt = bulkAiScoredAt; }
}
