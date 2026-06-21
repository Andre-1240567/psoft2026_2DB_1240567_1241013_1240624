package pt.isep.psoft.alsafe.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PingController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private void letFilterChainContinueWithoutAuthenticating() throws Exception {
        doAnswer((Answer<Void>) invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(authTokenFilter).doFilter(any(), any(), any());
    }

    private static RequestPostProcessor authenticatedAs(String username, String role) {
        return SecurityMockMvcRequestPostProcessors.user(username).roles(role);
    }

    @Test
    void ensureUnauthenticatedRequestToProtectedEndpointReturns401() throws Exception {
        letFilterChainContinueWithoutAuthenticating();

        mockMvc.perform(get("/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ensureAuthenticatedRequestToProtectedEndpointIsAllowedThrough() throws Exception {
        letFilterChainContinueWithoutAuthenticating();

        mockMvc.perform(get("/ping").with(authenticatedAs("atcc_jose", "ATCC")))
                .andExpect(status().isOk());
    }

    @Test
    void ensurePublicAuthEndpointIsAccessibleWithoutAuthentication() throws Exception {
        letFilterChainContinueWithoutAuthenticating();

        mockMvc.perform(get("/api/auth/ping"))
                .andExpect(status().isOk());
    }
}