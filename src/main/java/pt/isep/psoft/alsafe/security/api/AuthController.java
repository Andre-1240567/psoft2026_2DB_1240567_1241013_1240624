package pt.isep.psoft.alsafe.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.security.domain.SystemUser;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;
import pt.isep.psoft.alsafe.security.repositories.SystemUserRepository;

import java.util.Optional;

@Tag(name = "Authentication", description = "Login endpoint — returns a JWT token")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final SystemUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtils jwtUtils, SystemUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtils        = jwtUtils;
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated. Returns the JWT token."),
        @ApiResponse(responseCode = "400", description = "Invalid request body (e.g., missing username or password)."),
        @ApiResponse(responseCode = "401", description = "Invalid credentials (incorrect username or password).")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {

        Optional<SystemUser> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPasswordHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials.");
        }

        SystemUser user = userOpt.get();
        String jwt = jwtUtils.generateJwtToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
    }
}