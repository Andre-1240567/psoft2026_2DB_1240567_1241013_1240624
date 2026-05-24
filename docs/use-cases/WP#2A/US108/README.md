# US108 - Search Airports

## User Story
> As an ATCC or Backoffice Operator, I want to search airports by specific criteria (e.g., city).

## Acceptance Criteria
- The request must include search parameters in the URL (e.g., `?city=Name`).
- On success, the system returns HTTP 200 with a list of airports that match the criteria.

## Pre-conditions
- The actor is authenticated.

## Post-conditions
- None (Read-only operation).

## Main Success Scenario
1. The actor sends a `GET /api/airports?city={cityName}` request.
2. The system validates the presence of the search parameter.
3. The system queries the database for airports located in the specified city.
4. The system returns HTTP 200 OK with an array of matching airports.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | The search parameter is missing or empty | HTTP 400 Bad Request |
| 3 | No airports are found for the given criteria | HTTP 200 OK (with an empty list) |

## Design Justification
- **Spring Data Query Derivation:** The search leverages the `findByLocation_City` method derived automatically by Spring Data JPA to traverse the `Location` value object natively.

## Sequence Diagrams
- [System Sequence Diagram](puml/US108-SSD.puml)
- [Sequence Diagram](puml/US108-SD.puml)