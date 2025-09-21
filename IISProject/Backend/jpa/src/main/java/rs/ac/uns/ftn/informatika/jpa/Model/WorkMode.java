package rs.ac.uns.ftn.informatika.jpa.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "work_modes")
public class WorkMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public WorkMode() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}