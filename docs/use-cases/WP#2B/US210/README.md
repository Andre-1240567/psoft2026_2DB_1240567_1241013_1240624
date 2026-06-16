# US210 - Busiest Airports Statistics

## User Story
> As a Backoffice Operator, I want to generate statistics on the busiest airports by number of routes to aid in strategic planning.

## Acceptance Criteria
- The system must provide a list of airports sorted by the total number of routes (origin + destination).
- The results should be provided via a REST API.
- The results must use HATEOAS links.

## Pre-conditions
- The actor is authenticated as a Backoffice Operator.

## Post-conditions
- None (Read-only operation).

## Main Success Scenario
1. The actor sends a `GET /api/airports/statistics/busiest` request.
2. The system verifies that the actor has the required role.
3. The system aggregates route counts per airport via a single native database query.
4. The system maps the results into a lightweight DTO (`BusiestAirportDTO`).
5. The system returns HTTP 200 OK with a HATEOAS-enriched CollectionModel.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | The actor is not a Backoffice Operator | HTTP 403 Forbidden |

## Design Justification
- **Native Query Optimization:** To avoid the N+1 problem and massive memory overhead that would occur by fetching every route and airport into Java, the route aggregation is performed directly in the database using a Native SQL `UNION ALL` query.

### System Sequence Diagram
![System Sequence Diagram](puml/US210-SSD.puml)

### Sequence Diagram
![Sequence Diagram](puml/US210-SD.puml)