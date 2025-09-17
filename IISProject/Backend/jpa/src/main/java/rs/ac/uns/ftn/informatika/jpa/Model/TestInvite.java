package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_invites")
public class TestInvite {

    @Id @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type")
    private TestType testType;

    @Column(name = "test_url", length = 2048)
    private String testUrl;

    private OffsetDateTime deadline;
    private BigDecimal passThreshold;
    private String status;

    public TestInvite() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public TestType getTestType() { return testType; }
    public void setTestType(TestType testType) { this.testType = testType; }

    public OffsetDateTime getDeadline() { return deadline; }
    public void setDeadline(OffsetDateTime deadline) { this.deadline = deadline; }

    public BigDecimal getPassThreshold() { return passThreshold; }
    public void setPassThreshold(BigDecimal passThreshold) { this.passThreshold = passThreshold; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTestUrl() { return testUrl; }
    public void setTestUrl(String testUrl) { this.testUrl = testUrl; }
}
