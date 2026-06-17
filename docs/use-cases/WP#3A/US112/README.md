# US112 - Update or Deactivate a Flight Route

## 1. Requirements Engineering

### 1.1. User Story Description
As an ATCC or Backoffice Operator, I want to update or deactivate a flight route.

### 1.2. Customer Specifications and Clarifications
* **Deactivation (Soft Delete):** Routes cannot be physically deleted from the database to preserve operational history. Instead, they must be marked as `DEACTIVATED`. An already deactivated route cannot be deactivated again.
* **Update Constraints:** A deactivated route is considered "locked". Its technical parameters (distance, requirements) cannot be updated unless it is reactivated (out of scope for this US).
* **Concurrency Protection (Optimistic Locking):** Since multiple operators might attempt to edit the same route simultaneously, the system must employ Optimistic Locking. Clients must provide the `version` they are editing. If the database version is newer than the provided one, the request is rejected to prevent lost updates.

### 1.3. Acceptance Criteria
* **AC1:** A `PATCH` request to deactivate must transition the route status to `DEACTIVATED` and implicitly append a log to the route's history.
* **AC2:** Attempting to update or deactivate a route that is already `DEACTIVATED` must violate domain state rules and yield a `409 Conflict`.
* **AC3:** A `PUT` request to update a route must contain a `version` field. If this version does not match the current database version, the system must abort and return `409 Conflict` (Optimistic Locking Failure).
* **AC4:** All successful mutations (updates or deactivations) must automatically reflect in the route's historical audit trail and return a `200 OK` response enriched with HATEOAS links.

## 2. Analysis & Design

### 2.1. Pre-Conditions
* The target flight route must exist in the database.
* To perform any operation (`PUT` or `PATCH`), the route must currently be in an `ACTIVE` state.

### 2.2. Post-Conditions
* The route's state (`RouteStatus`) or parameters (`distance`, `RouteRequirement`) are mutated.
* The internal `version` field is incremented automatically by the persistence provider.
* A new `RouteHistory` record is generated within the aggregate.

### 2.3. Design Artifacts

* **System Sequence Diagram (SSD):** Maps both the update and deactivate interactions.
![US112 System Sequence Diagram](svg/US112-SSD.svg)

* **Sequence Diagram (SD):** Showcases both flows, highlighting the optimistic locking validation mechanism and domain invariant checks.
![US112 Sequence Diagram](svg/US112-SD.svg)

* **Class Diagram (CD):** Details the Aggregate Root structure and the DTO mapping strategy.
![US112 Class Diagram](svg/US112-CD.svg)