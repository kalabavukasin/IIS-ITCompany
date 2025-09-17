package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "workflow_stages")
public class WorkflowStage {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDef workflow;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private String name;

    private Integer slaDays;

    public WorkflowStage() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public WorkflowDef getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDef workflow) { this.workflow = workflow; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }
}
