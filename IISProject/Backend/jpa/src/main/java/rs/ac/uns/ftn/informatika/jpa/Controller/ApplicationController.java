package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.ApplicationDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.ApplicationService;

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
}
