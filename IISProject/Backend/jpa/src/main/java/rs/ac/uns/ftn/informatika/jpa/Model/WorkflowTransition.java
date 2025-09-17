package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RoleType;

import java.util.UUID;

@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransition {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDef workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id", nullable = false)
    private WorkflowStage fromStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stage_id", nullable = false)
    private WorkflowStage toStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "allowed_role", nullable = false)
    private RoleType allowedRole; // KO SME da izvrši tranziciju (ADMIN, HR_MANAGER, ...)

    @Lob
    private String conditionExprJson;

    @Lob
    private String autoActionsJson;

    public WorkflowTransition() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public WorkflowDef getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDef workflow) { this.workflow = workflow; }

    public WorkflowStage getFromStage() { return fromStage; }
    public void setFromStage(WorkflowStage fromStage) { this.fromStage = fromStage; }

    public WorkflowStage getToStage() { return toStage; }
    public void setToStage(WorkflowStage toStage) { this.toStage = toStage; }

    public String getConditionExprJson() { return conditionExprJson; }
    public void setConditionExprJson(String conditionExprJson) { this.conditionExprJson = conditionExprJson; }

    public String getAutoActionsJson() { return autoActionsJson; }
    public void setAutoActionsJson(String autoActionsJson) { this.autoActionsJson = autoActionsJson; }

    public RoleType getAllowedRole() { return allowedRole; }
    public void setAllowedRole(RoleType allowedRole) { this.allowedRole = allowedRole; }
}
