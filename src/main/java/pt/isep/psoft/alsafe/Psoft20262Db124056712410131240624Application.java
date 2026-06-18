package pt.isep.psoft.alsafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class Psoft20262Db124056712410131240624Application {

    public static void main(String[] args) {
        SpringApplication.run(Psoft20262Db124056712410131240624Application.class, args);
    }
}
