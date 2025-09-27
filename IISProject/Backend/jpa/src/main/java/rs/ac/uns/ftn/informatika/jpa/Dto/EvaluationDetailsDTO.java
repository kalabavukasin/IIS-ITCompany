package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.OffsetDateTime;

public record EvaluationDetailsDTO (
            Long id,
            String grade,
            String comment,
            OffsetDateTime createdAt
){}
