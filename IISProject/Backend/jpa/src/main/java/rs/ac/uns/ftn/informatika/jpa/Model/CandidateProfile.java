package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // optional

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "cv_original_name")
    private String cvOriginalName;

    @Column(name = "cv_mime")
    private String cvMime;

    @Column(name = "cv_size_bytes")
    private Long cvSizeBytes;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public CandidateProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public String getCvOriginalName() { return cvOriginalName; }
    public void setCvOriginalName(String cvOriginalName) { this.cvOriginalName = cvOriginalName; }

    public String getCvMime() { return cvMime; }
    public void setCvMime(String cvMime) { this.cvMime = cvMime; }

    public Long getCvSizeBytes() { return cvSizeBytes; }
    public void setCvSizeBytes(Long cvSizeBytes) { this.cvSizeBytes = cvSizeBytes; }

}