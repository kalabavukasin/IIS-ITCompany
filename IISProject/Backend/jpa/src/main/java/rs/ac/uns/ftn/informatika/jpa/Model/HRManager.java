package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

@Entity
@Table(name = "hr_managers")
public class HRManager extends User {

    private Long teamId;

    public HRManager() { setRole(Role.HR_MANAGER);}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
