package pt.isep.psoft.alsafe.security.api;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String username;
    private String password;
}