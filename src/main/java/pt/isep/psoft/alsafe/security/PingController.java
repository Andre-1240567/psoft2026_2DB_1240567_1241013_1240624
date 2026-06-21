package pt.isep.psoft.alsafe.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal controller that exists only to give {@link SecurityConfigTest} real
 * endpoints to send requests to.
 *
 * <p>Deliberately a top-level class (not nested inside the test class):
 * {@code @WebMvcTest(controllers = ...)} registers it as a Spring MVC
 * controller bean, and top-level classes avoid the ambiguity some Spring
 * Boot versions have when resolving statically-nested {@code @RestController}
 * classes declared inside a test class.
 *
 * <p>{@code /ping} is not part of {@code SecurityConfig}'s public allow-list,
 * so it falls under {@code anyRequest().authenticated()}. {@code /api/auth/ping}
 * matches the {@code /api/auth/**} allow-list and must be reachable without
 * authentication.
 */
@RestController
class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/api/auth/ping")
    public String authPing() {
        return "pong";
    }
}