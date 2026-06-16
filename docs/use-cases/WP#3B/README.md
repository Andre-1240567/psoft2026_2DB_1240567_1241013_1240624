# Work Package 3B (WP#3B) - Enhanced Route Management & Flight Operations

## 1. Overview
This Work Package extends the flight routes domain with advanced operational features,
including scheduled flight management, route analytics, and alternative route discovery.
It builds upon the foundation established in WP#3A.

## 2. Implemented User Stories

* **[US212 - Schedule a Flight](./US212/README.md):** Allows an ATCC to assign an aircraft
  to a route for a specific date and time. Enforces range, capacity, airport certification,
  aircraft availability, and time overlap constraints using Pessimistic Locking.

* **[US213 - View Scheduled Flights](./US213/README.md):** Enables an ATCC to retrieve all
  scheduled flights for a specific aircraft, or to fetch a specific flight by its number.

* **[US214 - List Active Routes Sorted](./US214/README.md):** Returns a paginated list of
  active routes sorted by popularity (number of assignments) or distance.

* **[US215 - Total Network Distance](./US215/README.md):** Calculates the sum of distances
  of all active routes in the network.

* **[US216 - Alternative Routes](./US216/README.md):** Finds alternative multi-leg route
  combinations between two airports using pluggable routing algorithms (Strategy Pattern).
  Default algorithm: `fewest-stops` (BFS). Additional algorithm: `eco-friendly` (DFS,
  minimises total distance).

## 3. Bonus Features

These features were implemented beyond the assignment scope to demonstrate
extended domain coverage and architectural depth.

* **[Bonus - Cancel Scheduled Flight](./Bonus/CancelFlight.md):** Allows an ATCC to cancel
  a scheduled flight, freeing the aircraft for reassignment. Cancelled flights are excluded
  from overlap detection queries (Pessimistic Lock query updated accordingly).

* **[Bonus - Departures Board](./Bonus/DeparturesBoard.md):** Returns upcoming departures
  from a specific airport within a configurable time window (default: 24h).

## 4. Non-Functional Requirements

- **Security (JWT):** All endpoints require JWT. `ATCC` role required for scheduling and
  cancellation; `ATCC` or `BACKOFFICE_OPERATOR` for the departures board.
- **HATEOAS:** Scheduled flight responses include navigational links.
- **Concurrency:** Pessimistic Locking (`@Lock`) prevents double-booking of aircraft.
  Optimistic Locking (`@Version`) on `ScheduledFlight` handles concurrent updates.
- **Validation:** Input DTOs validated with Bean Validation.
- **OpenAPI:** All endpoints documented via Swagger UI.

## 5. Design Artifacts

### Use Case Diagram (UCD)
![WP3B Use Case Diagram](svg/WP%233B-UCD.svg)

### Domain Model (DM)
![WP3B Domain Model](svg/WP%233B-DM.svg)

### State Machine Diagram — ScheduledFlight
Details the lifecycle of a `ScheduledFlight`: `SCHEDULED` → `CANCELED` or `COMPLETED`.
![WP3B State Machine Diagram](svg/WP%233B-SMD.svg)