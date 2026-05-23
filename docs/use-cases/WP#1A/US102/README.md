# US102 - Register Aircraft Instance

## User Story
> As an Air Transport Company Collaborator (ATCC), I want to register a new physical aircraft instance in the fleet.

## Acceptance Criteria
- The request must provide `registrationNumber`, `modelName`, `manufacturingDate`, and `activeConfigurationName`.
- The registration number must follow standard aviation regex formatting.
- The associated `modelName` must exist in the system.
- On success, the system returns HTTP 201 Created with the aircraft details and HATEOAS links.

## Pre-conditions
- The actor is authenticated as an `ATCC`.
- The referenced aircraft model must already exist in the database.

## Post-conditions
- A new `Aircraft` aggregate is persisted in the database, linked to its `AircraftModel`.

## Main Success Scenario
1. The actor sends a `POST /api/aircrafts` request with the aircraft payload.
2. The system validates the request fields (e.g., regex for registration number).
3. The system queries the database to find the existing aircraft model.
4. The system creates the `Aircraft` entity in memory.
5. The system persists the aircraft.
6. The system maps the entity to a DTO, adds HATEOAS links, and returns HTTP 201 Created.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Request payload is missing required fields or violates regex constraints | HTTP 400 Bad Request |
| 3 | The referenced aircraft model does not exist | HTTP 404 Not Found (or 400 Bad Request) |

## Design Justification
- **Domain-Driven Design (DDD):** The `Aircraft` relies on the existence of an `AircraftModel`, establishing a clear relationship between instances and their base specifications.
- **HATEOAS:** Rest Maturity Level 3 is applied by returning dynamic self-links (`_links`) pointing to the US103 endpoint.

## Sequence Diagrams
- [System Sequence Diagram](US102-SSD.puml)
- [Sequence Diagram](US102-SD.puml)