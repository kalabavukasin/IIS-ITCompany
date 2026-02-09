package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestion_id", nullable = false)
    private Requestion requestion;

    private LocalDate validFrom;
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "posting_status")
    private JobPostingStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_workflow_id")
    private WorkflowDef pipelineWorkflow;

    public JobPosting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Requestion getRequestion() { return requestion; }
    public void setRequestion(Requestion requisition) { this.requestion = requisition; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public JobPostingStatus getStatus() { return status; }
    public void setStatus(JobPostingStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public WorkflowDef getPipelineWorkflow() { return pipelineWorkflow; }
    public void setPipelineWorkflow(WorkflowDef pipelineWorkflow) { this.pipelineWorkflow = pipelineWorkflow; }

}
