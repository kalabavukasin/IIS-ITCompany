package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewType;

import java.time.OffsetDateTime;

public record InterviewToShowDTO (
    Long id,
    InterviewType type,
    String location,
    String candidateName,
    Integer duration,
    InterviewStatus status,
    OffsetDateTime scheduledAt
    ){}
