package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.airports.domain.Airport;
import pt.isep.psoft.alsafe.airports.repositories.AirportRepository;

@Component
public class Bootstrapper implements CommandLineRunner {

    // É aqui que injetamos os Repositórios maltinha, caso queiram usar o Bootstrap também
    private final AirportRepository airportRepository;
    
    // Construtor para o Spring Boot injetar as dependências automaticamente
    public Bootstrapper(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    // Este é o método do CommandLineRunner que corre mal a aplicação arranca
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        
        // Chamamos os métodos por ordem de dependência. 
        // (Ex: Só podes criar rotas se já existirem aeroportos!)
        bootstrapAirports();
        bootstrapFlightRoutes();
        bootstrapAircrafts();
        
        System.out.println("Bootstraper deployed!");
    }

    // MÉTODOS DE BOOTSTRAP ESPECÍFICOS (Um para cada WP)

    private void bootstrapAirports() {
        // Só injetamos dados se a tabela estiver vazia, para não criar duplicados se o servidor reiniciar
        if (airportRepository.count() == 0) {
            airportRepository.save(new Airport("OPO")); // Porto
            airportRepository.save(new Airport("LIS")); // Lisboa
            airportRepository.save(new Airport("CDG")); // Paris - Charles de Gaulle
            airportRepository.save(new Airport("MAD")); // Madrid
            
            System.out.println(" -> Airports loaded.");
        }
    }

    private void bootstrapFlightRoutes() {
        // Aqui vou colocar os teus testes da US110 mais tarde, 
        // ou deixar vazio por agora até ter o FlightRouteService pronto.
    }

    private void bootstrapAircrafts() {
        // Espaço reservado para vocês fazerem a vossa parte (WP#1 e WP#2)
        // Quando tiverem as entidades, só precisam de injetar o repositório lá em cima 
        // e adicionar os dados falsos aqui.
    }
}