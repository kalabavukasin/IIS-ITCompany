package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.*;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;
import rs.ac.uns.ftn.informatika.jpa.Util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping("/apply")
    public ApplicationDTO apply(@RequestParam Long postingId) {
        Long candidateId = SecurityUtils.getCurrentUserId();
        return service.apply(postingId, candidateId);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public List<ApplicationDTO> mine() {
        Long candidateId = SecurityUtils.getCurrentUserId();
        return service.listMine(candidateId);
    }

    @GetMapping("/my-cards")
    public ResponseEntity<List<ApplicationCardDTO>> myCards() {
        Long candidateId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getMyApplicationCards(candidateId));
    }

    @GetMapping("/cards")
    public List<ApplicationWithUserDTO> getAllCards() {
        return service.getAllCards();
    }

    @GetMapping("/cards/my-created")
    @PreAuthorize("hasAuthority('HR_MANAGER')")
    public List<ApplicationWithUserDTO> getCardsByCreatedByHr() {
        Long hrId = SecurityUtils.getCurrentUserId();
        return service.getCardsByCreatedByHr(hrId);
    }

    @GetMapping("/cards/my-managed")
    @PreAuthorize("hasAuthority('HIRING_MANAGER')")
    public List<ApplicationWithUserDTO> getCardsByHiringManager() {
        Long hmId = SecurityUtils.getCurrentUserId();
        return service.getCardsByHiringManager(hmId);
    }

    @GetMapping("/cards/by-posting/{postingId}")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER','HIRING_MANAGER')")
    public List<ApplicationWithUserDTO> getCardsByPosting(@PathVariable Long postingId) {
        return service.getCardsByPosting(postingId);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApplicationDetailsDTO> details(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetails(id));
    }

    @PostMapping("/{id}/refuse")
    public ResponseEntity<ApplicationDTO> refuse(@PathVariable Long id, @RequestBody RefuseRequestDTO body) {
        return ResponseEntity.ok(service.refuse(id, body.getReason()));
    }
}
