package rs.ac.uns.ftn.informatika.jpa.Controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.CreateEvaluationRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.EvaluationService;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    private final EvaluationService evalService;

    public EvaluationController(EvaluationService evalService) {
        this.evalService = evalService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateEvaluationRequestDTO body) {
        var res = evalService.create(body);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<?> getByInterviewAndInterviewer(@RequestParam Long interviewId,
                                                          @RequestParam Long interviewerId) {
        var res = evalService.getForInterviewAndInterviewer(interviewId, interviewerId);
        return ResponseEntity.ok(res);
    }
    @GetMapping("/by-application/{applicationId}/details")
    public ResponseEntity<?> getByApplication(@PathVariable Long applicationId) {
        var dto = evalService.getLatestForApplication(applicationId);
        return ResponseEntity.ok(dto);
    }
}
