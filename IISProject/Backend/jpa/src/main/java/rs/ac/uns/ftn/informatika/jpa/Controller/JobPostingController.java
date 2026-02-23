package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.PostingDateRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.JobPosting;
import rs.ac.uns.ftn.informatika.jpa.Service.JobPostingService;

@RestController
@RequestMapping("/api/postings")
public class JobPostingController {

    private final JobPostingService service;

    public JobPostingController(JobPostingService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingCardDTO> getPosting(@PathVariable Long id) {
        return service.findByIdWithRequestion(id)
                .map(this::toCard)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<JobPostingCardDTO> archive(@PathVariable Long id) {
        JobPosting p = service.archiveManually(id);
        return ResponseEntity.ok(toCard(p));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<JobPostingCardDTO> extend(
            @PathVariable Long id,
            @RequestBody PostingDateRequestDTO body) {
        if (body.newValidTo == null) {
            return ResponseEntity.badRequest().build();
        }
        JobPosting p = service.extendValidTo(id, body.newValidTo);
        return ResponseEntity.ok(toCard(p));
    }

    @PostMapping("/{id}/republish")
    public ResponseEntity<JobPostingCardDTO> republish(
            @PathVariable Long id,
            @RequestBody(required = false) PostingDateRequestDTO body) {
        JobPosting p = service.republish(id, body != null ? body.newValidTo : null);
        return ResponseEntity.ok(toCard(p));
    }

    private JobPostingCardDTO toCard(JobPosting p) {
        var r = p.getRequestion();
        return new JobPostingCardDTO(
                p.getId(),
                r.getName(),
                r.getDescription(),
                r.getLocation(),
                r.getBudget(),
                p.getValidTo(),
                p.getCreatedAt(),
                r.getSeniority() != null ? r.getSeniority().name() : null,
                p.getStatus().name()
        );
    }
}
