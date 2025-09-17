package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "hr_managers")
public class HRManager extends User {

    private UUID teamId;

    public HRManager() {}

    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }
}
