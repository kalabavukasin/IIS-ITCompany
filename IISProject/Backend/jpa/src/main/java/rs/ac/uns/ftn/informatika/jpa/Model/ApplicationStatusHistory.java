package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private WorkflowStage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id")
    private WorkflowTransition transition; // null for first entry

    @Column(nullable = false)
    private OffsetDateTime enteredAt;

    private OffsetDateTime exitedAt;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private User triggeredBy;

    public ApplicationStatusHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public WorkflowStage getStage() { return stage; }
    public void setStage(WorkflowStage stage) { this.stage = stage; }

    public WorkflowTransition getTransition() { return transition; }
    public void setTransition(WorkflowTransition transition) { this.transition = transition; }

    public OffsetDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(OffsetDateTime enteredAt) { this.enteredAt = enteredAt; }

    public OffsetDateTime getExitedAt() { return exitedAt; }
    public void setExitedAt(OffsetDateTime exitedAt) { this.exitedAt = exitedAt; }


    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public User getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(User triggeredBy) { this.triggeredBy = triggeredBy; }
}
