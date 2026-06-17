# US111 - Track Route History

## 1. Architectural Concept & Analysis

### 1.1. User Story Description
As an ATCC, I want to keep track of route history, viewing the chronological log of all modifications made to a specific flight route.

### 1.2. Design Approach Note
Tracking route history involves two distinct architectural behaviors:

1. **Writing History (Implicit Domain Concern):** There is no direct user interaction or REST endpoint specifically designated to "create a history log". The lifecycle auditing mechanism is entirely encapsulated within the Domain Layer. Whenever a mutation event occurs on a `FlightRoute` entity (such as initial Creation, Field Updates, or Deactivation), the domain model implicitly triggers `addHistory()`. This guarantees that no state change can ever bypass the audit trail.
2. **Reading History (Explicit REST Endpoint):** To fulfill the ATCC's need to "keep track" of the history, an explicit `GET /api/flight-routes/{id}/history` endpoint is provided to fetch the immutable list of `RouteHistory` entries.

### 1.3. Acceptance Criteria
* **AC1:** Every entry in the history log must record the exact timestamp, the description of the change, and the authenticated author responsible for the modification.
* **AC2:** The history collection must be append-only, ensuring structural immutability (logs cannot be altered, spoofed, or deleted).
* **AC3:** If no user authentication context is present during system operations (e.g., automated bootstrap), the author fields defaults to `"System"`.
* **AC4:** The system must return a `404 Not Found` if the requested route does not exist.
* **AC5:** The response must be a `200 OK` containing the list of history entries and valid HATEOAS links.

## 2. Design & Implementation Artifacts

### 2.1. System Sequence Diagram (SSD)
Illustrates the ATCC requesting the history log and the system returning the payload.
*(Refer to `US111-SSD.puml`)*

### 2.2. Sequence Diagram (SD)
Details the application flow. Notably, the repository uses a `LEFT JOIN FETCH` to load the `@ElementCollection` of history records eagerly, avoiding `LazyInitializationException` during the DTO mapping phase.
*(Refer to `US111-SD.puml`)*

### 2.3. Class Diagram (CD)
The class design establishes a strict composition (`*--`) relationship between the `FlightRoute` Aggregate Root and the `RouteHistory` Value Object. A `RouteHistory` instance cannot exist outside the context of its parent route.
*(Refer to `US111-CD.puml`)*