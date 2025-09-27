package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.Dto.StaffMemberDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserService userService, PasswordEncoder passwordEncoder){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id){
        var u = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found " + id));
        return new UserDTO(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),u.getPhone());
    }
    @PatchMapping("/{id}/phone")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public UserDTO updatePhone(@PathVariable Long id, @RequestBody PhoneDTO body) {
        var u = userService.getUserById(id).orElseThrow();
        u.setPhone(body.phone());
        userService.updateUser(u);
        return new UserDTO(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone());
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    public void changePassword(@PathVariable Long id, @RequestBody ChangePwdDTO body) {
        var u = userService.getUserById(id).orElseThrow();
        if (!passwordEncoder.matches(body.oldPassword(), u.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        u.setPassword(passwordEncoder.encode(body.newPassword()));
        userService.updateUser(u);
    }
    @GetMapping("/staff")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER', 'HIRING_MANAGER')")
    public ResponseEntity<List<StaffMemberDTO>> getStaffMembers() {
        List<StaffMemberDTO> staffMembers = userService.getStaffMembers();
        return ResponseEntity.ok(staffMembers);
    }

    public record UserDTO(Long id, String firstName, String lastName, String email, String phone) {}
    public record PhoneDTO(String phone) {}
    public record ChangePwdDTO(String oldPassword, String newPassword) {}
}
