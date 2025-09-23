package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_stages")
public class WorkflowStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDef workflow;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private String name;

    public WorkflowStage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowDef getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDef workflow) { this.workflow = workflow; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
