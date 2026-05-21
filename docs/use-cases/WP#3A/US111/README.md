# US111 - Track Route History

## 1. Architectural Concept & Analysis

### 1.1. User Story Description
As an ATCC, I want to keep track of route history.

### 1.2. Design Approach Note (Crucial Architecture Constraint)
Unlike typical CRUD functionalities, **tracking route history does not possess a standalone, user-facing REST endpoint**. There is no direct user interaction or system command specifically designated to "create a history log". 

Instead, this is treated as a **Cross-Cutting Domain Concern**. The lifecycle auditing mechanism is entirely encapsulated within the Domain Layer. Whenever a mutation event occurs on a `FlightRoute` entity (such as initial Creation, Field Updates, or Deactivation), the domain model implicitly triggers `addHistory()`. This guarantees that no state change can ever bypass the audit trail.

### 1.3. Acceptance Criteria
* **AC1:** Every entry in the history log must record the exact timestamp, the route's current status, and the authenticated author responsible for the modification.
* **AC2:** The history collection must be append-only, ensuring structural immutability (logs cannot be altered, spoofed, or deleted).
* **AC3:** If no user authentication context is present during system operations (e.g., automated bootstrap), the author fields defaults to `"System"`.

## 2. Design & Implementation Artifacts

### 2.1. Domain Sequence Diagram (SD)
Since this feature is entirely decoupled from external HTTP routing, the sequence diagram strictly models the inner domain boundaries and how the `FlightRoute` aggregate root manages its internal `@ElementCollection` of `RouteHistory` value objects during a mutation event (e.g., Deactivation).

*(Refer to `US111-SD.puml` for the structural sequence interaction).*

### 2.2. Class Diagram (CD)
The class design establishes a strict composition (`*--`) relationship between `FlightRoute` and `RouteHistory`. A `RouteHistory` instance cannot exist outside the context of its parent route.

*(Refer to `US111-CD.puml` for class associations and method visibility).*