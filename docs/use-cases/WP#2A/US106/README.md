# US106 - Create Airport

## User Story
> As a Backoffice Operator, I want to create an airport so that flight routes can be established and managed.

## Acceptance Criteria
- The request must provide `iataCode`, `name`, `location` (region, country, city), `gpsCoordinates` (latitude, longitude), `timezone` and `runways`.
- The IATA Code must be exactly 3 uppercase letters and unique in the system.
- The timezone must be a valid timezone string (e.g., "UTC+01:00").
- On success, the system returns HTTP 201 with the created Airport details.

## Pre-conditions
- The actor is authenticated as a Backoffice Operator.

## Post-conditions
- A new `Airport` entity is persisted in the database.
- The airport is automatically created with the `OPERATIONAL` status.
- The created airport becomes available for flight route definitions and searches.

## Main Success Scenario
1. The actor sends a `POST /api/airports` request with the airport payload.
2. The system validates the request fields.
3. The system checks if an airport with the given IATA code already exists.
4. The system instantiates the Value Objects and creates the `Airport` aggregate.
5. The system persists the airport and returns HTTP 201 Created with the detailed DTO.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Request payload is missing required fields or invalid formats | HTTP 400 Bad Request |
| 3 | An airport with the provided IATA code already exists | HTTP 409 Conflict (or 400) |

## Design Justification
- **Domain-Driven Design (DDD):** `Airport` acts as the Aggregate Root. Concepts like `IATACode`, `Location`, `GPSCoordinates`, and `Timezone` were modeled as Value Objects (`@Embeddable`) to encapsulate validation and prevent Primitive Obsession.
- **Transactional Consistency:** The creation of the airport and its runways is wrapped in a `@Transactional` boundary in the `AirportService`.

## Sequence Diagrams
- [System Sequence Diagram](puml/US106-SSD.puml)
- [Sequence Diagram](puml/US106-SD.puml)