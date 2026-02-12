package rs.ac.uns.ftn.informatika.jpa.Controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingDetailDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.CustomUserDetails;
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
    public JobPostingDetailDTO get(@PathVariable Long id) {
        Long candidateId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            candidateId = details.getUser().getId();
        }
        return service.getDetail(id, candidateId);
    }
}
