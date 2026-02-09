package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class JobPostingCardDTO {
    public Long id;
    public String name;          // iz Requestion.name
    public String description;   // iz Requestion.description (kratko)
    public String location;      // iz Requestion.location
    public BigDecimal salary;        // iz Requestion.budget
    public LocalDate expires;    // JobPosting.validTo
    public OffsetDateTime createdAt;

    public JobPostingCardDTO(Long id, String name, String description, String location,
                             BigDecimal salary, LocalDate expires, OffsetDateTime createdAt) {
        this.id = id; this.name = name; this.description = description;
        this.location = location; this.salary = salary;
        this.expires = expires; this.createdAt = createdAt;
    }
}
