package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.TestType;

import java.time.OffsetDateTime;
import java.util.List;

public class BulkTestInviteDTO {
    public List<Long> applicationIds;
    public TestType type;
    public OffsetDateTime activeUntil;
}
