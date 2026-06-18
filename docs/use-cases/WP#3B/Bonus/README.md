# WP#3B - Bonus Features & Extra User Stories

This directory contains the documentation, design decisions, and PlantUML diagrams for the bonus features and extra User Stories implemented as part of Work Package 3B for the AISafe project.

These features go beyond the core requirements to provide a more robust, realistic, and feature-rich Flight Management System.

## Extra User Stories

* **[US223 - Aircraft Utilization Rates](./US223/README.md)** Calculates monthly flight hours and total flights per aircraft.
* **[US227 - Fuel Efficiency Metrics](./US227/README.md)** Estimates fuel consumption and efficiency (km/L) per aircraft and per route.
* **[US228 - Export Route Network Data](./US228/README.md)** Exports active flight routes in standard aviation and mapping formats (`GeoJSON` and `KML`).
* **[US229 - Route Utilization Report](./US229/README.md)** Generates an administrative report of routes ranked by the total number of scheduled flights.

## Additional System Features

* **[Cancel Flight](./CancelFlight/README.md)** Provides the logic and endpoints to safely cancel a scheduled flight, updating its status without deleting the record.
* **[Departures Board](./DeparturesBoard/README.md)** Generates a dynamic departures board for a specific airport within a configurable time window (e.g., next 24 or 48 hours).
* **[Get Flight By ID](./GetFlightById/README.md)** Retrieves detailed HATEOAS-enriched information about a specific scheduled flight using its unique flight number.