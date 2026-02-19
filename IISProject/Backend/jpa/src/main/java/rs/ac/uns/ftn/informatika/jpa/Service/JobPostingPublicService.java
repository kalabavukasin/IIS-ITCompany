package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingCardDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.JobPostingDetailDTO;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobPostingPublicService {
    private final JobPostingRepository repo;
    private final ApplicationService applicationService;

    public JobPostingPublicService(JobPostingRepository repo, ApplicationService applicationService) {
        this.repo = repo;
        this.applicationService = applicationService;
    }

    @Transactional(readOnly = true)
    public List<JobPostingCardDTO> listOpen() {
        var today = LocalDate.now();
        return repo.findOpenWithRequestion(today).stream()
                .map(p -> new JobPostingCardDTO(
                        p.getId(),
                        p.getRequestion().getName(),
                        p.getRequestion().getDescription(),
                        p.getRequestion().getLocation(),
                        p.getRequestion().getBudget(),
                        p.getValidTo(),
                        p.getCreatedAt(),
                        p.getRequestion().getSeniority() != null ? p.getRequestion().getSeniority().name() : null
                ))
                .toList();
    }
    @Transactional(readOnly = true)
    public JobPostingDetailDTO getDetail(Long postingId, Long candidateIdOrNull) {
        var p = repo.findByIdWithRequestion(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found " + postingId));

        boolean applied = false;
        if (candidateIdOrNull != null) {
            applied = applicationService.hasApplied(postingId, candidateIdOrNull);
        }

        var r = p.getRequestion();
        return new JobPostingDetailDTO(
                p.getId(),
                r.getName(),
                r.getDescription(),
                r.getLocation(),
                r.getSeniority() != null ? r.getSeniority().name() : null,
                r.getSkills(),
                r.getBudget(),
                p.getValidTo(),
                applied
        );
    }
}
