package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User {
    public Admin() {}
}
