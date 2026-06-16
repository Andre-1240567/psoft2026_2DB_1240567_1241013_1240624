# US211 - View Airports Grouped by Region or Country

## User Story
> As an ATCC, I want to view airports grouped by region or country to visualize geographical distribution.

## Acceptance Criteria
- The system must provide a list of airports grouped by the specified criteria (region or country).
- The response must be a Map where the keys are the regions/countries and the values are lists of airports.
- The response must support HATEOAS representation models.

## Pre-conditions
- The actor is authenticated as an ATCC.

## Post-conditions
- None (Read-only operation).

## Main Success Scenario
1. The actor sends a `GET /api/airports/grouped?groupBy={criteria}` request.
2. The system verifies that the actor has the required role.
3. The system fetches all registered airports from the database.
4. The system groups the airports by the requested criteria using Java Streams.
5. The system transforms the raw entities into `AirportViewDTO` models and injects HATEOAS links via the `AirportModelAssembler`.
6. The system returns HTTP 200 OK with the grouped map.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 4 | The `groupBy` parameter is invalid or missing | HTTP 400 Bad Request |
| 2 | The actor is not an ATCC | HTTP 403 Forbidden |

## Design Justification
- **In-Memory Grouping:** Given the relatively static and manageable size of the airport dataset, using Java's `Collectors.groupingBy` provides a clean, highly readable, and easily extensible way to group by embedded `Location` attributes without resorting to complex native SQL mapping.

### System Sequence Diagram
![System Sequence Diagram](puml/US211-SSD.puml)

### Sequence Diagram
![Sequence Diagram](puml/US211-SD.puml)