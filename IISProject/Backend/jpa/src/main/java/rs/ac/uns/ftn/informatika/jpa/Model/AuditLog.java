package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "ix_audit_time", columnList = "timeUtc"))
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false) private String action;
    @Column(nullable = false) private String entityType;
    @Column(nullable = false) private Long entityId;

    @Lob private String beforeDataJson;
    @Lob private String afterDataJson;

    private String source;
    private String ipAddr;
    private String userAgent;
    private String requestId;

    @Column(nullable = false)
    private OffsetDateTime timeUtc;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getBeforeDataJson() { return beforeDataJson; }
    public void setBeforeDataJson(String beforeDataJson) { this.beforeDataJson = beforeDataJson; }

    public String getAfterDataJson() { return afterDataJson; }
    public void setAfterDataJson(String afterDataJson) { this.afterDataJson = afterDataJson; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public OffsetDateTime getTimeUtc() { return timeUtc; }
    public void setTimeUtc(OffsetDateTime timeUtc) { this.timeUtc = timeUtc; }
}
