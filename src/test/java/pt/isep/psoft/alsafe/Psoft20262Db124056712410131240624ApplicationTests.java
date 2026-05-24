package pt.isep.psoft.alsafe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;

@SpringBootTest
class Psoft20262Db124056712410131240624ApplicationTests {

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void contextLoads() {
    }

}