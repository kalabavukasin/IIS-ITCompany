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
    @JoinColumn(name = "requisition_id", nullable = false)
    private Requestion requisition;

    @Column(nullable = false)
    private String title;

    @Lob @Column(nullable = false)
    private String description;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "work_mode_id")
    private WorkMode workMode;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "language_id")
    private Language language;

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

    private Integer pipelineWorkflowVersion;

    public JobPosting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Requestion getRequisition() { return requisition; }
    public void setRequisition(Requestion requisition) { this.requisition = requisition; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

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

    public Integer getPipelineWorkflowVersion() { return pipelineWorkflowVersion; }
    public void setPipelineWorkflowVersion(Integer pipelineWorkflowVersion) { this.pipelineWorkflowVersion = pipelineWorkflowVersion; }
}
