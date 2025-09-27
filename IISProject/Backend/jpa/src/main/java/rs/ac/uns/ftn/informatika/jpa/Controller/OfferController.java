package rs.ac.uns.ftn.informatika.jpa.Controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.AcceptOfferRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.OfferService;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/{candidateId}/recent")
    public ResponseEntity<?> recent(@PathVariable Long candidateId,
                                    @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(offerService.recentForCandidate(candidateId, days));
    }
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<?> accept(@PathVariable Long offerId,
                                    @Valid @RequestBody AcceptOfferRequestDTO body) {
        var dto = offerService.acceptOffer(offerId, body.userId());
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/{offerId}/decline")
    public ResponseEntity<?> decline(@PathVariable Long offerId,
                                     @Valid @RequestBody AcceptOfferRequestDTO body) {
        var dto = offerService.declineOffer(offerId, body.userId());
        return ResponseEntity.ok(dto);
    }
}
