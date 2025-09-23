package rs.ac.uns.ftn.informatika.jpa.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Seniority;

import java.math.BigDecimal;

public class CreateRequestionDTO {
    @NotBlank public String positionInFirm;
    @NotBlank public String description;
    @NotBlank public String programmingLanguages; // "Java, Spring, Angular"
    @NotNull public Seniority seniority;
    @NotBlank public String location;
    @NotNull  @DecimalMin("0.0") public BigDecimal budget;
    @NotBlank public String name;
}
