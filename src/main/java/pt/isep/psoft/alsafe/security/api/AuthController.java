package pt.isep.psoft.alsafe.security.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequestDTO loginRequest) {

        String role = "USER";

        if ("atcc".equals(loginRequest.getUsername())) {
            role = "ATCC";
        } else if ("operator".equals(loginRequest.getUsername())) {
            role = "BACKOFFICE_OPERATOR";
        } else if ("admin".equals(loginRequest.getUsername())) {
            role = "ADMIN,BACKOFFICE_OPERATOR,ATCC";
        } else {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }

        String jwt = jwtUtils.generateJwtToken(loginRequest.getUsername(), role);

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
    }
}