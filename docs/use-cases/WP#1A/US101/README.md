# US101 - Register Aircraft Model

## User Story
> As a Backoffice Operator, I want to register a new aircraft model in the system so that I can later instantiate physical aircraft belonging to this model.

## Acceptance Criteria
- The request must provide `manufacturer`, `modelName`, `seatingCapacity`, `fuelCapacity`, `maxRange`, and `cruisingSpeed`.
- All performance and capacity metrics must be strictly positive values.
- The `manufacturer` must belong to a predefined enumeration of valid manufacturers.
- On success, the system returns HTTP 201 Created with the model details.

## Pre-conditions
- The actor is authenticated as a `BACKOFFICE_OPERATOR`.

## Post-conditions
- A new `AircraftModel` entity is persisted in the database.
- The aircraft model becomes available for the creation of specific aircraft instances.

## Main Success Scenario
1. The actor sends a `POST /api/aircraft-models` request with the model payload.
2. The system validates the request fields and constraints.
3. The system creates the `AircraftModel` entity in memory.
4. The system persists the model in the database.
5. The system returns HTTP 201 Created with the detailed DTO.

## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Request payload is missing fields, has negative metrics, or an invalid manufacturer | HTTP 400 Bad Request |

## Design Justification
- **Domain-Driven Design (DDD):** `AircraftModel` acts as an Entity in our domain.
- **Security & Authorization:** The endpoint is secured as a cross-cutting concern using Spring Security JWT (`@PreAuthorize`).
- **DTO Pattern:** Inputs and outputs are isolated from domain entities using `CreateAircraftModelDTO` to avoid exposing the internal domain structure.

## Sequence Diagrams
- [System Sequence Diagram](US101-SSD.puml)
- [Sequence Diagram](US101-SD.puml)