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
    @NotBlank public String programmingLanguages;
    @NotNull public Seniority seniority;
    @NotBlank public String location;
    @NotNull  @DecimalMin("0.0") public BigDecimal budget;
    @NotBlank public String name;
    @NotNull public Long pipelineWorkflowId;

    @NotNull public Integer durationDays;
    @NotNull public LocalDate reviewDeadline;
    @NotNull public Integer minExperienceYears;
    @NotBlank public String niceToHaveSkills;
}
