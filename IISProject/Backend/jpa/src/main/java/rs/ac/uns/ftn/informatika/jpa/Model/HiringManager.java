package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "hiring_managers")
public class HiringManager extends User {

    private UUID departmentId;

    public HiringManager() {}

    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
}