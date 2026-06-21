# Work Package 4 (WP#4) - Maintenance Records Management

## 1. Overview
This Work Package implements the maintenance domain for the AISafe Flight
Management System: reusable maintenance templates, individual maintenance
records tied to specific aircraft, completion workflows, fleet-wide reporting,
and proactive due-maintenance alerting. It is split into two phases — WP#4A
(core maintenance creation and tracking) and WP#4B (search, reporting, and
alerting on top of the core model).

## 2. Implemented User Stories — WP#4A

* **[US115A - Create Maintenance Template](./US115A/README.md):** Allows a
  Maintenance Technician to define a reusable template (name, type, default
  duration, applicable aircraft models, and checklist) that standardizes
  recurring maintenance work.

* **[US115B - Create Maintenance Record](./US115B/README.md):** Opens a new
  maintenance record for a specific aircraft from an existing template. The
  template's checklist is deep-copied into the record, the template's
  applicability to the aircraft's model is validated, and the aircraft is
  automatically transitioned to `UNDER_MAINTENANCE`.

* **[US116 - View Maintenance Records by Aircraft](./US116/README.md):**
  Returns the full maintenance history (all statuses) for a given aircraft
  registration number.

* **[US117 - Total Maintenance Hours per Aircraft](./US117/README.md):**
  Reports the sum of expected maintenance hours per aircraft across the
  fleet, sorted descending, for fleet-wide workload planning.

* **[US119 - Mark Maintenance Record as Completed](./US119/README.md):**
  Transitions an `IN_PROGRESS` record to `COMPLETED` with completion notes,
  actual duration, and actual cost. Restores the aircraft to `AVAILABLE`.

## 3. Implemented User Stories — WP#4B

* **[US217 - Categorize by Maintenance Component](./US217/README.md):** Not
  a standalone endpoint — every maintenance record is mandatorily categorized
  by `MaintenanceComponent` (ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR)
  at creation time (US115B), and that categorization is consumed by US218
  and US219.

* **[US218 - Search Maintenance Records](./US218/README.md):** Flexible
  multi-filter search by aircraft, maintenance component, and/or start date
  range, using a single dynamic JPQL query with optional `IS NULL OR ...`
  parameters.

* **[US219 - View Ongoing Maintenance Activities](./US219/README.md):**
  Fleet-wide, unfiltered view of every record currently `PLANNED` or
  `IN_PROGRESS`, sorted by start date — a supervisory dashboard.

* **[US220 - Maintenance Cost Reports](./US220/README.md):** Two reports —
  estimated vs. actual cost aggregated per aircraft, and per aircraft model —
  for budget tracking and variance analysis.

* **[US221 - Average Turnaround per Aircraft Type](./US221/README.md):**
  Average **actual** maintenance duration grouped by aircraft model, computed
  only from `COMPLETED` records, for fleet performance benchmarking.

* **[US222 - Scheduled Maintenance Alerts](./US222/README.md):** Two
  independent alert feeds — aircraft due by calendar date, and aircraft due
  by accumulated flight hours — surfacing maintenance that needs to be
  scheduled before it becomes a safety issue.

## 4. Non-Functional Requirements

- **Security (JWT):** All endpoints require JWT. `MAINTENANCE_TECHNICIAN` role
  required for creating/updating templates and records and for state
  transitions (start/complete/cancel); `MAINTENANCE_SUPERVISOR` for fleet-wide
  oversight (US219, US221); `ATCC` for fleet-level reports and alerts
  (US117, US220, US222). Read endpoints for individual/aircraft-scoped
  records (US116, search, get-by-id) are open to Technician, Supervisor, and
  ATCC alike.
- **HATEOAS:** Maintenance record and template responses include
  navigational links via dedicated model assemblers.
- **Concurrency:** Optimistic Locking (`@Version`) on both `MaintenanceRecord`
  and `MaintenanceTemplate`. State-changing endpoints (`start`, `complete`,
  `cancel`, template `update`) require the client to supply the current
  version, returning 409 Conflict on a stale write — mirroring the pattern
  established in WP#3B's `FlightRouteService`.
- **Validation:** Input DTOs validated with Bean Validation
  (`@NotBlank`, `@NotNull`, `@Positive`, `@PositiveOrZero`); business
  invariants (state transition legality, template-aircraft compatibility,
  non-negative costs) enforced directly in the domain layer.
- **OpenAPI:** All endpoints documented via Swagger UI with detailed
  per-status-code descriptions.

## 5. Design Artifacts

### Use Case Diagram (UCD)
![WP4 Use Case Diagram](svg/WP%234-UCD.svg)

### Domain Model (DM)
![WP4 Domain Model](svg/WP%234-DM.svg)

### State Machine Diagram — MaintenanceRecord
Details the lifecycle of a `MaintenanceRecord`:
`PLANNED` → `IN_PROGRESS` → `COMPLETED`, with `cancel()` available from
either `PLANNED` or `IN_PROGRESS`.
![WP4 State Machine Diagram](svg/WP%234-SMD.svg)

## 6. Test Coverage

Unit and repository-integration tests were written for all domain entities,
services, and custom repository queries. Full coverage report available in
[Quality-Assurance/](../WP%233B/Quality-Assurance/README.md) *(shared report
covering the whole codebase)*.

| Package | Scope |
|---|---|
| maintenancemanagement.domain | Entity invariants, state transitions (`@NoArgsConstructor(PROTECTED)`, rich behavior) |
| maintenancemanagement.services | Orchestration, optimistic locking, exception mapping |
| maintenancemanagement.repositories | Custom JPQL — aggregations, dynamic search filters, due-alert queries |
| maintenancemanagement.api | Controller request/response mapping, DTO validation |