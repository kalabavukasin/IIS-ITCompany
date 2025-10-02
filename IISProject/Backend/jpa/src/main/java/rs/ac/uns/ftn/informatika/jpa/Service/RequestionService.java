package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.Dto.CreateRequestionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.RequestionResponseDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Mapper.RequestionMapper;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Model.User;
import rs.ac.uns.ftn.informatika.jpa.Model.WorkflowDef;
import rs.ac.uns.ftn.informatika.jpa.Repository.RequestionRepository;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.Repository.WorkflowDefRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestionService {
    private final RequestionRepository repo;
    private final UserService userService;
    private final JobPostingService jobPostingService;
    private final WorkflowService workflowService;
    public RequestionService(RequestionRepository repo, UserService userService, JobPostingService jobPostingService,
                             WorkflowService workflowService) {
        this.repo = repo;
        this.userService = userService;
        this.jobPostingService = jobPostingService;
        this.workflowService = workflowService;
    }

    @Transactional
    public RequestionResponseDTO create(CreateRequestionDTO dto, Long userId) {
        Requestion r = RequestionMapper.toEntity(dto);
        User creator = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
       /* User hiring = userService.getUserById(Long.valueOf(3))
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));*/
        User hiring = userService.pickHiringManager();
        //requestion.setHiringManager(hiring);
        WorkflowDef workflow = workflowService.getWorkflowById(dto.pipelineWorkflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found with id " + dto.pipelineWorkflowId));

        r.setCreatedBy(creator);
        r.setHiringManager(hiring);
        r.setPipelineWorkflow(workflow);
        return RequestionMapper.toDto(repo.save(r));
    }
    public List<RequestionResponseDTO> list() {
        return repo.findAll().stream().map(RequestionMapper::toDto).collect(Collectors.toList());
    }
    public List<RequestionResponseDTO> listByCreator(Long userId) {
        return repo.findByCreatedBy_IdOrderByCreatedAtDesc(userId)
                .stream().map(RequestionMapper::toDto).collect(Collectors.toList());
    }

    public List<RequestionResponseDTO> listForHiringManager(Long hmId) {
        var statuses = List.of(RequestionStatus.DRAFT, RequestionStatus.PENDING_APPROVAL, RequestionStatus.APPROVED, RequestionStatus.REJECTED);
        return repo.findByHiringManager_IdAndStatusInOrderByCreatedAtDesc(hmId, statuses)
                .stream().map(RequestionMapper::toDto).collect(Collectors.toList());
    }
    public RequestionResponseDTO getById(Long id) {
        var r = repo.findById(id).orElseThrow(() -> new RuntimeException("Request not found: " + id));
        return RequestionMapper.toDto(r);
    }

    @Transactional
    public RequestionResponseDTO approve(Long id, Long hiringManagerId,String comment) {
        Requestion r = repo.findById(id).orElseThrow(() -> new RuntimeException("Request not found: " + id));
        if (r.getStatus() == RequestionStatus.APPROVED || r.getStatus() == RequestionStatus.REJECTED || r.getStatus() == RequestionStatus.CLOSED) {
            return RequestionMapper.toDto(r); // vec finalizovan — nista
        }
        User hm = userService.getUserById(hiringManagerId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + hiringManagerId));
        r.setHiringManager(hm);                     // ako vec nije setovano
        r.setStatus(RequestionStatus.APPROVED);
        r.setHiringComment(comment);

        jobPostingService.createForApprovedRequestion(r);

        return RequestionMapper.toDto(repo.save(r));
    }

    @Transactional
    public RequestionResponseDTO reject(Long id, Long hiringManagerId,String comment) {
        var r = repo.findById(id).orElseThrow(() -> new RuntimeException("Request not found: " + id));
        if (r.getStatus() == RequestionStatus.APPROVED || r.getStatus() == RequestionStatus.REJECTED || r.getStatus() == RequestionStatus.CLOSED) {
            return RequestionMapper.toDto(r);
        }
        var hm = userService.getUserById(hiringManagerId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + hiringManagerId));
        r.setHiringManager(hm);
        r.setStatus(RequestionStatus.REJECTED);
        r.setHiringComment(comment);
        return RequestionMapper.toDto(repo.save(r));
    }
}
