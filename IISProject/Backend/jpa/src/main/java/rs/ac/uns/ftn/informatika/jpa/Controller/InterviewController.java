package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.BulkInterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Interview;
import rs.ac.uns.ftn.informatika.jpa.Service.InterviewService;
import rs.ac.uns.ftn.informatika.jpa.Util.SecurityUtils;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER', 'HIRING_MANAGER')")
    public ResponseEntity<Void> scheduleInterview(@RequestBody InterviewScheduleDTO dto) {
        Long scheduledById = SecurityUtils.getCurrentUserId();
        Interview interview = interviewService.scheduleInterview(dto, scheduledById);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-schedule")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER', 'HIRING_MANAGER')")
    public ResponseEntity<Void> bulkSchedule(@RequestBody BulkInterviewScheduleDTO dto) {
        Long scheduledById = SecurityUtils.getCurrentUserId();
        interviewService.bulkSchedule(dto, scheduledById);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-application/{applicationId}/details")
    public ResponseEntity<?> getInterviewDetailsByApplication(@PathVariable Long applicationId) {
        return interviewService.getDetailsByApplicationId(applicationId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<?> getInterviewsByInterviewerId() {
        Long interviewerId = SecurityUtils.getCurrentUserId();
        var result = interviewService.getInterviewsByInterviewerId(interviewerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/observed")
    public ResponseEntity<?> getObservedInterviews() {
        Long userId = SecurityUtils.getCurrentUserId();
        var result = interviewService.getObservedInterviewsByUserId(userId);
        return ResponseEntity.ok(result);
    }
}
