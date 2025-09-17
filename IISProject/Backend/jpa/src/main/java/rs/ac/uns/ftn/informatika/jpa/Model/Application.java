package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.ApplicationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uq_application_candidate_job",
                columnNames = {"candidate_id", "job_posting_id"}))
public class Application {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateProfile candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDef workflow;

    @Column(nullable = false)
    private Integer workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id", nullable = false)
    private WorkflowStage currentStage;

    @Column(nullable = false)
    private OffsetDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "application_status")
    private ApplicationStatus status; // UI label (optional)

    @Lob private String note;

    public Application() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting jobPosting) { this.jobPosting = jobPosting; }

    public CandidateProfile getCandidate() { return candidate; }
    public void setCandidate(CandidateProfile candidate) { this.candidate = candidate; }

    public WorkflowDef getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDef workflow) { this.workflow = workflow; }

    public Integer getWorkflowVersion() { return workflowVersion; }
    public void setWorkflowVersion(Integer workflowVersion) { this.workflowVersion = workflowVersion; }

    public WorkflowStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(WorkflowStage currentStage) { this.currentStage = currentStage; }

    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(OffsetDateTime appliedAt) { this.appliedAt = appliedAt; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
