package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewScheduleDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Interview;
import rs.ac.uns.ftn.informatika.jpa.Service.InterviewService;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER', 'HIRING_MANAGER')")
    public ResponseEntity<Void> scheduleInterview(@PathVariable Long id,
            @RequestBody InterviewScheduleDTO dto) {
        Interview interview = interviewService.scheduleInterview(dto, id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/by-application/{applicationId}/details")
    public ResponseEntity<?> getTestDetailsByApplication(@PathVariable Long applicationId) {
        return interviewService.getDetailsByApplicationId(applicationId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
