package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

@Entity
@Table(name = "hiring_managers")
public class HiringManager extends User {

    private Long departmentId;

    public HiringManager() { setRole(Role.HIRING_MANAGER); }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}