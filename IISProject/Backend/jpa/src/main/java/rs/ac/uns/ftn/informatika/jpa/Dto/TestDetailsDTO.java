package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestInviteStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TestDetailsDTO (
        Long id,                       // TestInvite id ili test id
        TestType type,                 // npr. ONLINE/ONSITE...
        TestInviteStatus inviteStatus, // npr. SENT/PENDING/ACCEPTED...,
        OffsetDateTime deadline,
        String link,

        Boolean passed,
        BigDecimal score
){}
