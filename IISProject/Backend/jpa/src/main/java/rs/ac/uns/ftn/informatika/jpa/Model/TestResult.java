package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_invite_id", nullable = false, unique = true)
    private TestInvite testInvite;

    private BigDecimal score;
    private Boolean passed;

    @Lob
    private String metadataJson;

    public TestResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TestInvite getTestInvite() { return testInvite; }
    public void setTestInvite(TestInvite testInvite) { this.testInvite = testInvite; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
