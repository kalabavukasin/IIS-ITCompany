package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id", nullable = false)
    private WorkflowStage fromStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stage_id", nullable = false)
    private WorkflowStage toStage;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_transition_roles", joinColumns = @JoinColumn(name = "transition_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> allowedRoles = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String conditionExprJson;

    public WorkflowTransition() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowStage getFromStage() { return fromStage; }
    public void setFromStage(WorkflowStage fromStage) { this.fromStage = fromStage; }

    public WorkflowStage getToStage() { return toStage; }
    public void setToStage(WorkflowStage toStage) { this.toStage = toStage; }

    public String getConditionExprJson() { return conditionExprJson; }
    public void setConditionExprJson(String conditionExprJson) { this.conditionExprJson = conditionExprJson; }

    public Set<Role> getAllowedRoles() {
        return allowedRoles;
    }

    public void allowRole(Role role) {
        if (role != null) allowedRoles.add(role);
    }

    public void disallowRole(Role role) {
        if (role != null) allowedRoles.remove(role);
    }

    public void setAllowedRoles(Set<Role> roles) {
        this.allowedRoles.clear();
        if (roles != null) this.allowedRoles.addAll(roles);
    }
}
