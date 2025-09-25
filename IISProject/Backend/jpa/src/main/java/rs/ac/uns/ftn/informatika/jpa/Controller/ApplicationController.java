package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDetailsDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationWithUserDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }
    @PostMapping("/apply")
    public ApplicationDTO apply(@RequestParam Long postingId, @RequestParam Long candidateId) {
        return service.apply(postingId, candidateId);
    }
    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public List<ApplicationDTO> mine(@RequestParam Long candidateId) {
        return service.listMine(candidateId); // vrati DTO liste
    }
    @GetMapping("/{candidateId}/cards")
    public ResponseEntity<List<ApplicationCardDTO>> myCards(@PathVariable Long candidateId) {
        return ResponseEntity.ok(service.getMyApplicationCards(candidateId));
    }
    @GetMapping("/cards")
    public List<ApplicationWithUserDTO> getAllCards() {
        return service.getAllCards();
    }
    @GetMapping("/{id}/details")
    public ResponseEntity<ApplicationDetailsDTO> details(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetails(id));
    }
    /*@PostMapping("/{id}/advance")
    public ResponseEntity<Void> advance(@PathVariable Long id) {
        cmd.advanceToNextStage(id);
        return ResponseEntity.noContent().build();
    }*/

    /*@PostMapping("/{id}/refuse")
    public ResponseEntity<Void> refuse(@PathVariable Long id) {
        cmd.refuse(id);
        return ResponseEntity.noContent().build();
    }*/
}
