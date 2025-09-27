package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class InterviewScheduleDTO {
    public Long applicationId;
    public BigDecimal testScore; // optional - only if coming from Test phase
    public OffsetDateTime scheduledAt;
    public String location;
    public String interviewType; // ONSITE, ONLINE, PHONE
    public Integer durationMinutes;
    public Long interviewerId; // required - one interviewer
    public List<Long> observerIds; // optional - multiple observers

    public InterviewScheduleDTO() {}

}
