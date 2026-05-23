# US104 - Search Aircraft

## User Story
> As an ATCC, I want to search the aircraft fleet based on various parameters (e.g., model name, status, or manufacturing year).

## Acceptance Criteria
- The system must accept optional query parameters: `modelName`, `status`, and `year`.
- The system must dynamically filter the fleet based on the provided parameters.
- On success, the system returns HTTP 200 OK with a list of matching aircraft, each containing its own HATEOAS link.

## Pre-conditions
- The actor is authenticated as an `ATCC`.

## Post-conditions
- None (Read-only operation).

## Main Success Scenario
1. The actor sends a `GET /api/aircrafts` request with optional query parameters.
2. The system dynamically queries the repository using the provided filters.
3. The system iterates over the result list, mapping each entity to a DTO.
4. The system appends specific HATEOAS self-links to each element in the list.
5. The system returns HTTP 200 OK with the collection.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 1 | The provided query parameters have invalid data types (e.g., text instead of integer for year) | HTTP 400 Bad Request |

## Design Justification
- **Dynamic Querying:** The service layer abstracts the combination of optional parameters, utilizing Spring Data JPA's capabilities.
- **Iterative HATEOAS:** Ensures that lists are fully compliant with REST Level 3, allowing clients to navigate directly to any specific aircraft in the response.

## Sequence Diagrams
- [System Sequence Diagram](US104-SSD.puml)
- [Sequence Diagram](US104-SD.puml)