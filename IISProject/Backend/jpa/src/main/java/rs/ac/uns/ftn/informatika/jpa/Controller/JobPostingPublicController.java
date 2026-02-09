package rs.ac.uns.ftn.informatika.jpa.Controller;


import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingDetailDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.JobPostingPublicService;

import java.util.List;

@RestController
@RequestMapping("/api/postings/public")
public class JobPostingPublicController {
    private final JobPostingPublicService service;

    public JobPostingPublicController(JobPostingPublicService service) {
        this.service = service;
    }

    @GetMapping("/open")
    public List<JobPostingCardDTO> open() {
        return service.listOpen();
    }
    @GetMapping("/{id}")
    public JobPostingDetailDTO get(@PathVariable Long id, @RequestParam(required = false) Long candidateId) {
        return service.getDetail(id, candidateId);
    }
}
