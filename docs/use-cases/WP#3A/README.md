# Work Package 3A (WP#3A) - Flight Routes Management

## 1. Overview
This Work Package encapsulates the domain logic, services, and APIs required to manage
Flight Routes within the system. It handles the complete lifecycle of a flight route,
including creation, constraint validation, modification, deactivation, and historical auditing.

## 2. Implemented User Stories

* **[US110 - Create Flight Route](./US110/README.md):** Allows an ATCC to create a route
  by specifying origin and destination airports, estimated flight time, distance, and minimum
  aircraft requirements (range and capacity). Both airports must exist and be `OPERATIONAL`.

* **[US111 - Track and View Route History](./US111/README.md):** Every mutating operation
  on a route (creation, update, deactivation) automatically appends an audited history entry.
  History is also exposed as a dedicated read endpoint (`GET /{id}/history`) accessible by ATCC.

* **[US112 - Update or Deactivate Route](./US112/README.md):** Allows an ATCC or Backoffice
  Operator to update route parameters or deactivate a route. Updates are protected by
  Optimistic Locking — the client must supply the current version to prevent concurrent conflicts.
  Deactivated routes are permanently locked from further mutation.

* **[US113 - View Route Details](./US113/README.md):** Enables an ATCC to fetch the full
  details of a specific route by its unique ID, or to retrieve a paginated list of all routes
  departing from or arriving at a specific airport.

* **[US114 - Search Routes](./US114/README.md):** Paginated, case-insensitive search
  filtering routes by origin IATA code, destination IATA code, or both simultaneously.
  Returns HATEOAS-enriched results with pagination navigation links.

## 3. Non-Functional Requirements

The following cross-cutting concerns are enforced across all endpoints in this Work Package:

- **Security (JWT):** All endpoints require a valid JWT token. Role-based access control is
  enforced via `@PreAuthorize` — creation and read operations are restricted to `ATCC`;
  update and deactivation are available to `ATCC` and `BACKOFFICE_OPERATOR`.
- **HATEOAS:** All responses include navigational links (`self`, `history`, `deactivate`,
  `update`) built dynamically based on the route's current state. Paginated responses use
  `PagedResourcesAssembler` to inject `first`, `prev`, `next`, and `last` links.
- **Concurrency:** Optimistic Locking (`@Version`) is implemented end-to-end on `FlightRoute`.
  Version mismatches return `409 Conflict`.
- **Validation:** All input DTOs are validated with Bean Validation (`@NotBlank`, `@Pattern`,
  `@Positive`). IATA codes are enforced as exactly 3 capital letters.
- **OpenAPI:** All endpoints are documented with `@Tag` and `@Operation` annotations,
  available via the Swagger UI.

## 4. Global Design Artifacts (WP Level)

### Use Case Diagram (UCD)
Maps actors to their available operations, including the `<<include>>` relationship for
automatic history tracking on every mutating use case.
![WP3A Use Case Diagram](svg/WP%233A-UCD.svg)

### Domain Model (DM)
Represents the conceptual entities, value objects, and aggregates of the Flight Routes
subdomain, decoupled from technical implementation details.
![WP3A Domain Model](svg/WP%233A-DM.svg)

### State Machine Diagram (SMD)
Details the strict lifecycle of a `FlightRoute` entity — from creation (`ACTIVE`) through
optional mutation to permanent deactivation (`DEACTIVATED`) — and the guards enforced at
each transition.
![WP3A State Machine Diagram](svg/WP%233A-SMD.svg)