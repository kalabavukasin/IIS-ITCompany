package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class JobPostingDetailDTO {
    public Long id;
    public String name;
    public String description;
    public String location;
    public String seniority;
    public String programmingLanguages;
    public BigDecimal budget;
    public LocalDate openUntil;        // validTo
    public boolean alreadyApplied;     // popunjava se za datog kandidata

    public JobPostingDetailDTO(Long id, String name, String description, String location,
                               String seniority, String programmingLanguages, BigDecimal budget,
                               LocalDate openUntil, boolean alreadyApplied) {
        this.id = id; this.name = name; this.description = description; this.location = location;
        this.seniority = seniority; this.programmingLanguages = programmingLanguages;
        this.budget = budget; this.openUntil = openUntil; this.alreadyApplied = alreadyApplied;
    }
}
