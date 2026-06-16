package pt.isep.psoft.alsafe.security.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.psoft.alsafe.security.domain.SystemUser;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;
import pt.isep.psoft.alsafe.security.repositories.SystemUserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private SystemUserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void ensureLoginReturns200WithValidCredentials() throws Exception {
        SystemUser user = new SystemUser("atcc_jose", "hashed_password", "ATCC");

        when(userRepository.findByUsername("atcc_jose")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtils.generateJwtToken("atcc_jose", "ATCC")).thenReturn("mocked.jwt.token");

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("atcc_jose");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    void ensureLoginReturns401WhenUserNotFound() throws Exception {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("unknown");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ensureLoginReturns401WhenPasswordIsWrong() throws Exception {
        SystemUser user = new SystemUser("atcc_jose", "hashed_password", "ATCC");

        when(userRepository.findByUsername("atcc_jose")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("atcc_jose");
        request.setPassword("wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ensureLoginReturns400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}