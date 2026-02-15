package rs.ac.uns.ftn.informatika.jpa.Controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.CreateRequestionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.DecisionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.RequestionResponseDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.RequestionService;
import rs.ac.uns.ftn.informatika.jpa.Util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/requestions")
public class RequestionController {
    private final RequestionService service;
    public RequestionController(RequestionService service){ this.service = service; }

    @PostMapping
    public RequestionResponseDTO create(@RequestBody @Valid CreateRequestionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return service.create(dto, userId);
    }

    @GetMapping
    public List<RequestionResponseDTO> list() {
        return service.list();
    }

    @GetMapping("/mine")
    public List<RequestionResponseDTO> mine() {
        Long userId = SecurityUtils.getCurrentUserId();
        return service.listByCreator(userId);
    }

    @GetMapping("/to-approve")
    public List<RequestionResponseDTO> toApprove() {
        Long userId = SecurityUtils.getCurrentUserId();
        return service.listForHiringManager(userId);
    }

    @GetMapping("/{id}")
    public RequestionResponseDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/approve")
    public RequestionResponseDTO approve(@PathVariable Long id,
                                         @RequestBody @Valid DecisionDTO body) {
        Long userId = SecurityUtils.getCurrentUserId();
        return service.approve(id, userId, body.comment);
    }

    @PostMapping("/{id}/reject")
    public RequestionResponseDTO reject(@PathVariable Long id,
                                        @RequestBody @Valid DecisionDTO body) {
        Long userId = SecurityUtils.getCurrentUserId();
        return service.reject(id, userId, body.comment);
    }
}
