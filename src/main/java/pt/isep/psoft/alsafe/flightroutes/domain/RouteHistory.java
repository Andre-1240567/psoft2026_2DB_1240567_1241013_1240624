package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// @Embeddable diz que isto não é uma tabela independente, mas sim um objeto que pertence à Rota
@Embeddable
@Getter
@NoArgsConstructor
public class RouteHistory {
    
    private LocalDateTime changeDate;
    private String description;

    public RouteHistory(String description) {
        this.changeDate = LocalDateTime.now(); // Fica com a data e hora exata deste momento
        this.description = description;
    }
}