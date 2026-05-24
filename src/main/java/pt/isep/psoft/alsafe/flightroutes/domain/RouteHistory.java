package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor
public class RouteHistory {

    private LocalDateTime changeDate;
    private String description;
    private String author;

    public RouteHistory(String description, String author) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("History description cannot be blank.");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be blank.");
        }
        this.changeDate = LocalDateTime.now();
        this.description = description;
        this.author = author;
    }
}