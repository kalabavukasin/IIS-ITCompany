package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.jpa.Service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){ this.userService = userService; }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id){
        var u = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found " + id));
        return new UserDTO(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail());
    }

    public record UserDTO(Long id, String firstName, String lastName, String email) {}
}
