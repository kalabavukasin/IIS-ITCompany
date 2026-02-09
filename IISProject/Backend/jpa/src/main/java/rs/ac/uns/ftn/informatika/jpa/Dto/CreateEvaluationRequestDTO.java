package rs.ac.uns.ftn.informatika.jpa.Dto;

import jakarta.validation.constraints.*;

public record CreateEvaluationRequestDTO (
    @NotNull Long interviewId,
    @NotNull Long interviewerId,
    @NotBlank @Pattern(regexp = "^(10|[1-9])$") String grade,
    @Size(max = 4000) String comment
){}
