# US113 - View Routes by Airport or by ID

## 1. Requirements Engineering

### 1.1. User Story Description
As an ATCC, I want to view all routes from a specific airport, and to view the details of a route given its ID.

### 1.2. Customer Specifications and Clarifications
* **View by ID:** The system must allow fetching the exact details of a single flight route using its unique identifier.
* **View by Origin Airport:** The system must list all existing flight routes departing from a specified IATA code.
* **Format:** The returned data must include the route's constraints, distance, time, and current status.

### 1.3. Acceptance Criteria
* **AC1:** A `GET` request with a valid Route ID must return a `200 OK` and the complete route details in JSON format (including HATEOAS links).
* **AC2:** A `GET` request with an invalid Route ID must return a `404 Not Found`.
* **AC3:** A `GET` request querying by an origin IATA code must return a paginated list of routes departing from that specific airport.

## 2. Analysis & Design

### 2.1. Pre-Conditions
* The queried routes and airports must exist in the database.

### 2.2. Post-Conditions
* No data is mutated. The system safely returns the requested domain data mapped into a Data Transfer Object (DTO).

### 2.3. Design Artifacts
* **System Sequence Diagram (SSD):**
![US113 System Sequence Diagram](svg/US113-SSD.svg)

* **Sequence Diagram (SD):** Shows the flow for fetching a route by its ID.
![US113 Sequence Diagram](svg/US113-SD.svg)

* **Class Diagram (CD):** (Shared with US114).
![US113/114 Class Diagram](svg/US113-CD.svg)