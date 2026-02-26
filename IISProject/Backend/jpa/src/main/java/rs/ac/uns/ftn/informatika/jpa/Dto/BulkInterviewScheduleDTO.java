package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.OffsetDateTime;
import java.util.List;

public class BulkInterviewScheduleDTO {
    public List<Long> applicationIds;
    public String interviewType;
    public String location;
    public Integer durationMinutes;
    public OffsetDateTime firstScheduledAt;
    public Integer breakMinutes;
    public Long interviewerId;
    public List<Long> observerIds;
    public List<BulkTestScoreEntryDTO> testScores;
}
