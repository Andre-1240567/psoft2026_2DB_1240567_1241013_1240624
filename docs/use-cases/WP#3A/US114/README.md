# US114 - Search Flight Routes (Origin, Destination, or Both)

## 1. Requirements Engineering

### 1.1. User Story Description
As an ATCC or Backoffice Operator, I want to search for routes by origin, destination, or both.

### 1.2. Customer Specifications and Clarifications
* **Dynamic Search:** The query parameters must be optional. The system must adapt its search based on whether the user provided only the Origin, only the Destination, both, or neither.
* **Tolerance to Input (UX Check):** The system must be tolerant to case-insensitivity (e.g., searching for `opo` should automatically resolve to `OPO`).
* **Performance:** The system must implement pagination to avoid database overload when returning large sets of routes.

### 1.3. Acceptance Criteria
* **AC1:** The system must support optional query parameters (`originIata`, `destinationIata`).
* **AC2:** Inputs must be sanitized to uppercase before querying the database to ensure case-insensitivity.
* **AC3:** The system must return a `PagedModel` object containing HATEOAS links and pagination metadata (`page`, `size`, `totalElements`).
* **AC4:** The mapping from Domain Entities to DTOs, including the appending of RESTful links, must be strictly handled at the API/Presentation layer using Model Assemblers.

## 2. Analysis & Design

### 2.1. Pre-Conditions
* The queried routes must exist in the database.
* The operating user must be authenticated with the `ATCC` or `BACKOFFICE_OPERATOR` role.

### 2.2. Post-Conditions
* No data is mutated. A paginated structure containing the matching routes is returned cleanly.

### 2.3. Design Artifacts

* **System Sequence Diagram (SSD):**
![US114 System Sequence Diagram](svg/US114-SSD.svg)

* **Sequence Diagram (SD):** Showcases the dynamic repository routing, case-insensitivity logic, and the pagination mapping using `PagedResourcesAssembler`.
![US114 Sequence Diagram](svg/US114-SD.svg)

* **Class Diagram (CD):** Highlights the strict decoupling between the Service layer (returning Domain objects) and the Controller layer (using Assemblers for DTOs).
![US114 Class Diagram](svg/US114-CD.svg)