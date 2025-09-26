package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Seniority;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "requestions")
public class Requestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_hr_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id")
    private User hiringManager;

    @Column(nullable = false)
    private String positionTitle;

    @NotBlank @Column(nullable = false)
    private String description;

    @NotBlank
    private String skills;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority")
    private Seniority seniority;

    @Column(name = "location")
    private String location;

    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "requestion_status")
    private RequestionStatus status = RequestionStatus.DRAFT;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private String hiringComment;
    @NotBlank @Column(nullable = false,length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_workflow_id")
    private WorkflowDef pipelineWorkflow;

    public Requestion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getHiringManager() { return hiringManager; }
    public void setHiringManager(User hiringManager) { this.hiringManager = hiringManager; }

    public String getPositionTitle() { return positionTitle; }
    public void setPositionTitle(String positionTitle) { this.positionTitle = positionTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public Seniority getSeniority() { return seniority; }
    public void setSeniority(Seniority seniority) { this.seniority = seniority; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public RequestionStatus getStatus() { return status; }
    public void setStatus(RequestionStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getHiringComment() { return hiringComment; }
    public void setHiringComment(String hiringComment) { this.hiringComment = hiringComment; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WorkflowDef getPipelineWorkflow() { return pipelineWorkflow; }
    public void setPipelineWorkflow(WorkflowDef pipelineWorkflow) { this.pipelineWorkflow = pipelineWorkflow; }
}
