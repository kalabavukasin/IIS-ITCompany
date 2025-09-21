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

    @Lob
    @Column(name = "cv_data")
    private byte[] cvData;

    @Column(name = "cv_filename")
    private String cvFilename;

    @Column(name = "cv_content_type")
    private String cvContentType;

    //@Lob private String note;
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

    //public String getNote() { return note; }
    //public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public byte[] getCvData() {
        return cvData;
    }

    public String getCvFilename() {
        return cvFilename;
    }

    public String getCvContentType() {
        return cvContentType;
    }

    public void setCvData(byte[] cvData) {
        this.cvData = cvData;
    }

    public void setCvFilename(String cvFilename) {
        this.cvFilename = cvFilename;
    }

    public void setCvContentType(String cvContentType) {
        this.cvContentType = cvContentType;
    }
}