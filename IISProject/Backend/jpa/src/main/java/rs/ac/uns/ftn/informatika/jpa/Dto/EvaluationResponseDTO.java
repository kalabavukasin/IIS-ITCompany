package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.OffsetDateTime;

public record EvaluationResponseDTO(
        Long id,
        Long interviewId,
        Long interviewerId,
        String grade,
        String comment,
        OffsetDateTime createdAt
) {}
