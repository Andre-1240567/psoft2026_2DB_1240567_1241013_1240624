# US209 - View Routes by Airport

## User Story
> As an ATCC, I want to view all routes that depart from or arrive at a specific airport so that I can monitor regional traffic.

## Acceptance Criteria
- The system must provide a list of routes where the specified airport is either the origin or the destination.
- The response must be paginated.
- The response must include HATEOAS links for navigation.

## Pre-conditions
- The actor is authenticated as an ATCC.
- The target airport exists in the system.

## Post-conditions
- None (Read-only operation).

## Main Success Scenario
1. The actor sends a `GET /api/airports/{iataCode}/routes` request with optional pagination parameters.
2. The system verifies that the actor has the required role.
3. The system validates the airport's existence.
4. The system queries the `FlightRouteRepository` for routes matching the IATA code as either origin or destination.
5. The system returns HTTP 200 OK with a paginated, HATEOAS-enriched response using `PagedResourcesAssembler`.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 3 | The target airport does not exist | HTTP 404 Not Found |
| 2 | The actor is not an ATCC | HTTP 403 Forbidden |

## Design Justification
- **Cross-Subdomain Interaction:** The endpoint correctly resides in `AirportController` (Airport Management subdomain) but delegates the actual fetching logic to `FlightRouteService` (Flight Routes subdomain), ensuring that business logic boundaries are not bypassed.

### System Sequence Diagram
![System Sequence Diagram](puml/US209-SSD.puml)

### Sequence Diagram
![Sequence Diagram](puml/US209-SD.puml)