package pt.isep.psoft.alsafe.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.security.domain.SystemUser;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;
import pt.isep.psoft.alsafe.security.repositories.SystemUserRepository;

import java.util.Optional;

// #5, #6 — credentials now come from the database; password is verified with BCrypt
@Tag(name = "Authentication", description = "Login endpoint — returns a JWT token")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final SystemUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(JwtUtils jwtUtils, SystemUserRepository userRepository) {
        this.jwtUtils        = jwtUtils;
        this.userRepository  = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {

        Optional<SystemUser> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        // #5 — user must exist AND password must match the stored BCrypt hash
        if (userOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }

        SystemUser user = userOpt.get();
        String jwt = jwtUtils.generateJwtToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
    }
}