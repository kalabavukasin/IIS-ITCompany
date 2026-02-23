package rs.ac.uns.ftn.informatika.jpa.Service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.JobPostingStatus;
import rs.ac.uns.ftn.informatika.jpa.Model.JobPosting;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowDef;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.JobPostingRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowDefRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class JobPostingService {
    private final JobPostingRepository jobPostingRepo;
    private final WorkflowService workflowService;
    private final ApplicationRepository applicationRepo;

    public JobPostingService(JobPostingRepository jobPostingRepository,
                            WorkflowService workflowService,
                            ApplicationRepository applicationRepository) {
        this.jobPostingRepo = jobPostingRepository;
        this.workflowService = workflowService;
        this.applicationRepo = applicationRepository;
    }

    @Transactional
    public JobPosting createOrReactivateForApprovedRequestion(Requestion r) {
        Optional<JobPosting> existing = jobPostingRepo.findByRequestion_Id(r.getId());

        if (existing.isPresent()) {
            // Reactivate existing JobPosting
            JobPosting p = existing.get();
            p.setStatus(JobPostingStatus.PUBLISHED);
            p.setValidFrom(LocalDate.now());
            p.setValidTo(LocalDate.now().plusDays(30)); // Fresh 30 days
            return jobPostingRepo.save(p);
        } else {
            // Create new JobPosting
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
        }
    }

    @Transactional
    public JobPosting createForApprovedRequestion(Requestion r) {
        return createOrReactivateForApprovedRequestion(r);
    }

    @Transactional
    public void archiveJobPostingForRequestion(Long requestionId) {
        Optional<JobPosting> jobPosting = jobPostingRepo.findByRequestion_Id(requestionId);

        if (jobPosting.isPresent()) {
            JobPosting p = jobPosting.get();

            // Check if any applications exist
            boolean hasApplications = applicationRepo.existsByJobPosting_Id(p.getId());
            if (hasApplications) {
                throw new IllegalStateException(
                    "Cannot reject requestion - candidates have already applied to this job posting");
            }

            // Archive the job posting
            p.setStatus(JobPostingStatus.ARCHIVED);
            jobPostingRepo.save(p);
        }
    }

    public java.util.Optional<JobPosting> findByIdWithRequestion(Long postingId) {
        return jobPostingRepo.findByIdWithRequestion(postingId);
    }

    @Transactional
    public JobPosting archiveManually(Long postingId) {
        JobPosting p = jobPostingRepo.findByIdWithRequestion(postingId)
                .orElseThrow(() -> new IllegalArgumentException("Posting not found: " + postingId));
        if (p.getStatus() != JobPostingStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED postings can be archived");
        }
        p.setStatus(JobPostingStatus.ARCHIVED);
        return jobPostingRepo.save(p);
    }

    @Transactional
    public JobPosting extendValidTo(Long postingId, LocalDate newValidTo) {
        JobPosting p = jobPostingRepo.findByIdWithRequestion(postingId)
                .orElseThrow(() -> new IllegalArgumentException("Posting not found: " + postingId));
        if (p.getStatus() != JobPostingStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED postings can be extended");
        }
        if (!newValidTo.isAfter(p.getValidTo())) {
            throw new IllegalArgumentException(
                    "New expiry date must be after the current expiry date (" + p.getValidTo() + ")");
        }
        p.setValidTo(newValidTo);
        return jobPostingRepo.save(p);
    }

    @Transactional
    public JobPosting republish(Long postingId, LocalDate newValidTo) {
        JobPosting p = jobPostingRepo.findByIdWithRequestion(postingId)
                .orElseThrow(() -> new IllegalArgumentException("Posting not found: " + postingId));
        if (p.getStatus() != JobPostingStatus.ARCHIVED) {
            throw new IllegalStateException("Only ARCHIVED postings can be republished");
        }
        LocalDate today = LocalDate.now();
        if (p.getValidTo().isBefore(today)) {
            if (newValidTo == null) {
                throw new IllegalArgumentException(
                        "New expiry date is required — the current expiry date is in the past");
            }
            if (!newValidTo.isAfter(today)) {
                throw new IllegalArgumentException("New expiry date must be in the future");
            }
            p.setValidTo(newValidTo);
        } else if (newValidTo != null) {
            if (!newValidTo.isAfter(today)) {
                throw new IllegalArgumentException("New expiry date must be in the future");
            }
            p.setValidTo(newValidTo);
        }
        p.setStatus(JobPostingStatus.PUBLISHED);
        return jobPostingRepo.save(p);
    }
}
