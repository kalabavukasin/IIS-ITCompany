package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.jpa.Dto.WorkflowSummaryDto;
import rs.ac.uns.ftn.informatika.jpa.Service.WorkflowService;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/summaries")
    public List<WorkflowSummaryDto> listSummaries() {
        return workflowService.listWorkflowsWithStageNames();
    }
}
