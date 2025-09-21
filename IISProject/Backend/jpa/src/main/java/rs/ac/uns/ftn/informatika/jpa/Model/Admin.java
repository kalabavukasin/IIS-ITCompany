package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

@Entity
@Table(name = "admins")
public class Admin extends User {
    public Admin() {setRole(Role.ADMIN);}
}
