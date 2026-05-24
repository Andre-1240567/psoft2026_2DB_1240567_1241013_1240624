package pt.isep.psoft.alsafe.security.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
}