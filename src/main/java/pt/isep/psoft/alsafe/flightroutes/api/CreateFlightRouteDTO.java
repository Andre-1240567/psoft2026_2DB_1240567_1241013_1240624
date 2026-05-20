package pt.isep.psoft.alsafe.flightroutes.api;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateFlightRouteDTO {

    @NotBlank(message = "O código IATA de origem é obrigatório.")
    @Pattern(regexp = "[A-Z]{3}", message = "O código IATA de origem deve conter exatamente 3 letras maiúsculas.")
    private String originIata;

    @NotBlank(message = "O código IATA de destino é obrigatório.")
    @Pattern(regexp = "[A-Z]{3}", message = "O código IATA de destino deve conter exatamente 3 letras maiúsculas.")
    private String destinationIata;

    @NotNull(message = "A distância da rota é obrigatória.")
    @Positive(message = "A distância deve ser um valor estritamente positivo.")
    private Double distance;

    @NotNull(message = "O tempo estimado de voo é obrigatório.")
    @Positive(message = "O tempo estimado de voo deve ser superior a zero minutos.")
    private Integer estimatedFlightTime;

    @NotNull(message = "O alcance mínimo exigido para a aeronave é obrigatório.")
    @Positive(message = "O alcance mínimo exigido deve ser um valor positivo.")
    private Double minRangeRequired;

    @NotNull(message = "A capacidade mínima exigida para a aeronave é obrigatória.")
    @Positive(message = "A capacidade mínima exigida deve ser superior a zero lugares.")
    private Integer minCapacityRequired;
}