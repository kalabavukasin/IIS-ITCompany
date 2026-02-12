package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Service.OfferService;
import rs.ac.uns.ftn.informatika.jpa.Util.SecurityUtils;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/my-recent")
    public ResponseEntity<?> recent(@RequestParam(defaultValue = "30") int days) {
        Long candidateId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(offerService.recentForCandidate(candidateId, days));
    }

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<?> accept(@PathVariable Long offerId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var dto = offerService.acceptOffer(offerId, userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{offerId}/decline")
    public ResponseEntity<?> decline(@PathVariable Long offerId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var dto = offerService.declineOffer(offerId, userId);
        return ResponseEntity.ok(dto);
    }
}
