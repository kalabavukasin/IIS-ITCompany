package rs.ac.uns.ftn.informatika.jpa.Controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.Dto.CreateRequestionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.DecisionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.RequestionResponseDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.RequestionService;

import java.util.List;

@RestController
@RequestMapping("/api/requestions")
public class RequestionController {
    private final RequestionService service;
    public RequestionController(RequestionService service){ this.service = service; }

    @PostMapping
   // @PreAuthorize("hasAuthority('HR_MANAGER')")
    public RequestionResponseDTO create(@RequestParam("userId") Long userId, @RequestBody @Valid CreateRequestionDTO dto) {
        return service.create(dto, userId);
    }

    @GetMapping
    //@PreAuthorize("hasAnyAuthority('HR_MANAGER','ADMIN','HIRING_MANAGER')")
    public List<RequestionResponseDTO> list() {
        return service.list();
    }
    @GetMapping("/mine")
    //@PreAuthorize("hasAuthority('HR_MANAGER')")
    public List<RequestionResponseDTO> mine(@RequestParam Long userId) {
        return service.listByCreator(userId);
    }

    @GetMapping("/to-approve")
    //@PreAuthorize("hasAuthority('HIRING_MANAGER')")
    public List<RequestionResponseDTO> toApprove(@RequestParam Long userId) {
        return service.listForHiringManager(userId);
    }
    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyAuthority('HR_MANAGER','HIRING_MANAGER','ADMIN')")
    public RequestionResponseDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/approve")
    //@PreAuthorize("hasAuthority('HIRING_MANAGER')")
    public RequestionResponseDTO approve(@PathVariable Long id, @RequestParam Long userId,
                                         @RequestBody @Valid DecisionDTO body) {
        return service.approve(id, userId,body.comment);
    }

    @PostMapping("/{id}/reject")
   // @PreAuthorize("hasAuthority('HIRING_MANAGER')")
    public RequestionResponseDTO reject(@PathVariable Long id, @RequestParam Long userId,
                                        @RequestBody @Valid DecisionDTO body) {
        return service.reject(id, userId,body.comment);
    }
}
