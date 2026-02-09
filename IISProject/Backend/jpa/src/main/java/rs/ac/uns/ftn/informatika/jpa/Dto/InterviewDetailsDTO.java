package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewType;

import java.time.OffsetDateTime;

public record InterviewDetailsDTO (
        Long id,
        InterviewType type,
        OffsetDateTime scheduledAt,
        Integer durationMinutes,
        String location,
        InterviewStatus status

)
{ }
