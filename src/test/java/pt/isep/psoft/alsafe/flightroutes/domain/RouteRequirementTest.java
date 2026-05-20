package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteRequirementTest {

    @Test
    void ensureMustHavePositiveRangeAndCapacity() {
        // Arrange (Preparar) - Nada a preparar de especial neste caso
        
        // Act & Assert (Agir e Verificar)
        // O assertThrows verifica se o nosso código realmente atira a Exceção certa quando enviamos zeros ou negativos!
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RouteRequirement(0.0, -10); // Lotação de -10 pessoas!
        });

        // Verificar se a mensagem de erro que o sistema deita para fora é a correta
        assertEquals("O alcance e a capacidade devem ser maiores que zero.", exception.getMessage());
    }

    @Test
    void ensureValidRequirementsAreCreated() {
        // AssertDoesNotThrow garante que se mandarmos valores normais, o objeto é criado em paz
        assertDoesNotThrow(() -> {
            new RouteRequirement(1500.0, 100);
        });
    }
}