package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestType;

import java.time.OffsetDateTime;

public class TestInviteRequestDTO {
    public Long applicationId;
    public TestType type;
    public OffsetDateTime activeUntil;
    public Long triggeredById;

}
