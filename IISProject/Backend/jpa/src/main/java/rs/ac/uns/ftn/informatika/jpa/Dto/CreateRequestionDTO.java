package rs.ac.uns.ftn.informatika.jpa.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Seniority;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateRequestionDTO {
    @NotBlank public String positionInFirm;
    @NotBlank public String description;
    @NotBlank public String programmingLanguages; // "Java, Spring, Angular"
    @NotNull public Seniority seniority;
    @NotBlank public String location;
    @NotNull  @DecimalMin("0.0") public BigDecimal budget;
    @NotBlank public String name;
    @NotNull public Long pipelineWorkflowId;

    // optional enrichment fields
    public Integer durationDays;       // HR's preferred posting duration in days
    public LocalDate reviewDeadline;   // deadline for HM to review/approve
    public Integer minExperienceYears; // for AI scoring context
    public String niceToHaveSkills;    // for AI scoring context
}
