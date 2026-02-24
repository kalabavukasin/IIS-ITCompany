package rs.ac.uns.ftn.informatika.jpa.Dto;

import jakarta.validation.constraints.NotBlank;

public class DecisionDTO {
    @NotBlank
    public String comment;

    // HM can override the HR-requested posting duration at approval time (optional)
    public Integer durationDays;
}
