package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.JobPosting;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowDef;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowDefRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class JobPostingService {
    private final JobPostingRepository jobPostingRepo;
    private final WorkflowService workflowService;

    public JobPostingService(JobPostingRepository jobPostingRepository, WorkflowService workflowService) {
        this.jobPostingRepo = jobPostingRepository;
        this.workflowService = workflowService;
    }
    @Transactional
    public JobPosting createForApprovedRequestion(Requestion r) {
        return jobPostingRepo.findByRequestion_Id(r.getId())
                .orElseGet(() -> {
                    WorkflowDef wf = workflowService.getWorkflowById(r.getPipelineWorkflow().getId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "WorkflowDef not found or inactive for id=" + r.getPipelineWorkflow().getId()));

                    JobPosting p = new JobPosting();
                    p.setRequestion(r);
                    p.setValidFrom(LocalDate.now());
                    p.setValidTo(LocalDate.now().plusDays(30));
                    p.setCreatedAt(OffsetDateTime.now());
                    p.setStatus(JobPostingStatus.PUBLISHED);
                    p.setPipelineWorkflow(wf);
                    return jobPostingRepo.save(p);
                });
    }
}
