package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.Dto.LoginAndRegisterDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.StaffMemberDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.CandidateProfile;
import rs.ac.uns.ftn.informatika.jpa.Model.User;
import rs.ac.uns.ftn.informatika.jpa.Repository.CandidateProfileRepository;
import rs.ac.uns.ftn.informatika.jpa.Repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CandidateProfileRepository candidateProfileRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       CandidateProfileRepository candidateProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.candidateProfileRepository = candidateProfileRepository;
    }
    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
    public boolean emailExists(String email) { return userRepository.findByEmail(email).isPresent();}

    @Transactional(readOnly = true)
    public List<StaffMemberDTO> getStaffMembers() {
        List<Role> staffRoles = List.of(Role.HR_MANAGER, Role.HIRING_MANAGER, Role.INTERVIEWER);

        return userRepository.findByRoleIn(staffRoles).stream()
                .map(user -> new StaffMemberDTO(
                        user.getId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public User registerUser(User user, CandidateProfile candidateProfile)
    {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email je već zauzet.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate verification token and set user as inactive
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setActive(false);

        // Set default values
        user.setCreatedAt(OffsetDateTime.now()); // Postavi trenutni datum i vreme
        user.setRole(Role.CANDIDATE);

        // Save the user
        User savedUser = userRepository.save(user);
        candidateProfileRepository.save(candidateProfile);

        // Send the activation email
        emailService.sendActivationEmail(user.getEmail(), token);

        return savedUser;
    }
    public int activateUser(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getActive()) {
                user.setActive(true);
                user.setVerificationToken(null); // Clear token to prevent reuse
                userRepository.save(user);
                return 1; // Successfully activated
            } else {
                return -1; // User already active
            }
        } else {
            return 0; // Invalid token
        }
    }
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    public User pickHiringManager() {
        return userRepository.findHiringManagersOrderedByOpenRequestions(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active hiring manager found"));
    }
}
