package rs.ac.uns.ftn.informatika.jpa.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.Dto.LoginAndRegisterDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.CandidateProfile;
import rs.ac.uns.ftn.informatika.jpa.Model.CustomUserDetails;
import rs.ac.uns.ftn.informatika.jpa.Model.User;
import rs.ac.uns.ftn.informatika.jpa.Service.CvStorageService;
import rs.ac.uns.ftn.informatika.jpa.Service.UserService;
import rs.ac.uns.ftn.informatika.jpa.Util.JwtUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CvStorageService cvStorageService;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          CvStorageService cvStorageService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.cvStorageService = cvStorageService;
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestPart("data") LoginAndRegisterDTO.RegisterRequest dto,
            @RequestPart("cv") MultipartFile cv) throws IOException {

        Map<String, String> response = new HashMap<>();

        if (userService.emailExists(dto.email())) {
            response.put("error", "A user with this email already exists.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setPhone(dto.phone());

        CvStorageService.SavedCv saved = cvStorageService.save(cv);

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setFirstName(dto.firstName());
        profile.setLastName(dto.lastName());
        profile.setEmail(dto.email());
        profile.setPhone(dto.phone());
        profile.setCreatedAt(java.time.OffsetDateTime.now());

        if (cv != null && !cv.isEmpty()) {
            profile.setCvPath(saved.path);
            profile.setCvOriginalName(saved.originalName);
            profile.setCvMime(saved.mime);
            profile.setCvSizeBytes(saved.sizeBytes);
        }
        userService.registerUser(user,profile);
        response.put("message", "User registered successfully. Please check your email for activation link.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestParam("token") String token) {
        System.out.println("Activation endpoint hit with token: " + token); // Log each request
        Map<String, String> response = new HashMap<>();
        int activationResult = userService.activateUser(token);

        if (activationResult == 1) {
            response.put("message", "Account activated successfully. You can now log in.");
            return ResponseEntity.ok(response); // HTTP 200 for success
        } else if (activationResult == -1) {
            response.put("error", "This account has already been activated.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // HTTP 409 for already active
        } else {
            response.put("error", "Invalid or expired activation token.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // HTTP 400 for invalid token
        }
    }





    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginAndRegisterDTO.LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();  // Access custom User fields

            String token = jwtUtil.generateToken(authentication);

            response.put("token", token);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFirstName() + " " + user.getLastName());
            response.put("role", user.getRole().name());
            response.put("activated", user.getActive());
            response.put("message", "Login successful!");

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
