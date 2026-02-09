package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import org.hibernate.annotations.CreationTimestamp;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false)
    private String firstName;

    @NotBlank @Column(nullable = false)
    private String lastName;

    @Email @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @NotBlank @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isActive = false;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "verification_token")
    private String verificationToken;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CANDIDATE; // default za kandidate

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String passwordHash) { this.password = passwordHash; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setVerificationToken(String token) { this.verificationToken = token; }
    public String getVerificationToken() { return verificationToken; }

    public void setActive(boolean active) { this.isActive = active; }
    public boolean getActive() { return isActive; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
