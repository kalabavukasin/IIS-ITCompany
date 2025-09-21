package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewParticipantRole;


@Entity
@Table(name = "interview_participants")
public class InterviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_role")
    private InterviewParticipantRole roleOnInterview; // INTERVIEWER | OBSERVER

    public InterviewParticipant() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Interview getInterview() { return interview; }
    public void setInterview(Interview interview) { this.interview = interview; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public InterviewParticipantRole getRoleOnInterview() { return roleOnInterview; }
    public void setRoleOnInterview(InterviewParticipantRole roleOnInterview) { this.roleOnInterview = roleOnInterview; }
}
