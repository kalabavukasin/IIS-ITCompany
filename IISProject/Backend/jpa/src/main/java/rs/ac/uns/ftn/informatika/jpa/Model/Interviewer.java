package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Seniority;

@Entity
@Table(name = "interviewers")
public class Interviewer extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", nullable = false)
    private Seniority seniority;

    public Interviewer() {}

    public Seniority getSeniority() { return seniority; }
    public void setSeniority(Seniority seniority) { this.seniority = seniority; }
}