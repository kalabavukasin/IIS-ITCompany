package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class InterviewScheduleDTO {
    public Long applicationId;
    public BigDecimal testScore; // optional
    public OffsetDateTime scheduledAt;
    public String location;
    public String interviewType;
    public Integer durationMinutes;
    public Long interviewerId; // required
    public List<Long> observerIds; // optional

    public InterviewScheduleDTO() {}

}
