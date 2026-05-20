package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;

@Data
public class LinkDTO {
    private String href;
    private String rel;
    private String type;

    public LinkDTO(String href, String rel, String type) {
        this.href = href;
        this.rel = rel;
        this.type = type;
    }
}
