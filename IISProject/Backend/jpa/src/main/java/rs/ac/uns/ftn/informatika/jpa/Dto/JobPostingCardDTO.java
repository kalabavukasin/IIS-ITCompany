package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class JobPostingCardDTO {
    public Long id;
    public String name;          // from Requestion.name
    public String description;   // from Requestion.description
    public String location;      // from Requestion.location
    public BigDecimal salary;        // from Requestion.budget
    public LocalDate expires;    // JobPosting.validTo
    public OffsetDateTime createdAt;
    public String seniority;     // from Requestion.seniority

    public JobPostingCardDTO(Long id, String name, String description, String location,
                             BigDecimal salary, LocalDate expires, OffsetDateTime createdAt,
                             String seniority) {
        this.id = id; this.name = name; this.description = description;
        this.location = location; this.salary = salary;
        this.expires = expires; this.createdAt = createdAt;
        this.seniority = seniority;
    }
}
