package pt.isep.psoft.alsafe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // desligar o CSRF é obrigatório para conseguirmos fazer pedidos POST sem tokens via Postman!
            // thanks to Diogo lmao
            .csrf(AbstractHttpConfigurer::disable) 
            
            // Isto diz ao Spring "Por agora, deixa passar qualquer pedido HTTP" (teste)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() 
            );
            
        return http.build();
    }
}