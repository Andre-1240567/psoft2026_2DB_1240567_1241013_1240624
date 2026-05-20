package pt.isep.psoft.alsafe.flightroutes.api;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateFlightRouteDTO {

    @NotNull(message = "A distância da rota é obrigatória para a atualização.")
    @Positive(message = "A distância deve ser um valor estritamente positivo.")
    private Double distance;

    @NotNull(message = "O tempo estimado de voo é obrigatório para a atualização.")
    @Positive(message = "O tempo estimado de voo deve ser superior a zero minutos.")
    private Integer estimatedFlightTime;

    @NotNull(message = "O alcance mínimo exigido é obrigatório para a atualização.")
    @Positive(message = "O alcance mínimo exigido deve ser um valor positivo.")
    private Double minRangeRequired;

    @NotNull(message = "A capacidade mínima exigida é obrigatória para a atualização.")
    @Positive(message = "A capacidade mínima exigida deve ser superior a zero lugares.")
    private Integer minCapacityRequired;

    @NotNull(message = "A versão do registo é obrigatória para salvaguardar o controlo de concorrência.")
    private Long version;
}