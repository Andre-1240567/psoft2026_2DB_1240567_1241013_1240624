# Supplementary Specification

This document captures the system-wide Non-Functional Requirements (NFRs) for the AISafe Flight Management System, structured according to the FURPS+ model.

## Functionality
* **Security & Authentication:** The system relies on JSON Web Tokens (JWT) for stateless authentication. Access to specific endpoints is strictly controlled via Role-Based Access Control (RBAC). Roles include `ADMIN`, `BACKOFFICE_OPERATOR`, and `ATCC`.
* **Auditing (Soft Deletes):** To maintain operational history, critical data is never physically deleted. Routes are marked as `DEACTIVATED` and flights as `CANCELED`.

## Usability
* **API Documentation:** The REST API is fully documented and testable via a dynamically generated OpenAPI specification (Swagger UI).
* **HATEOAS Compliance:** The API achieves Level 3 of the Richardson Maturity Model. Responses include hypermedia links (`self`, `history`, `route`, etc.) to guide API consumers dynamically through available actions based on resource state.

## Reliability
* **Concurrency Management (Optimistic):** Aggregate Roots (`Aircraft`, `FlightRoute`, `ScheduledFlight`) employ Optimistic Locking (via an `@Version` field) to prevent lost updates during concurrent edits.
* **Concurrency Management (Pessimistic):** Critical business transactions, such as scheduling a flight, utilize Pessimistic Write Locks at the database level to strictly prevent race conditions (e.g., double-booking an aircraft for overlapping time slots).
* **Validation:** All incoming data is rigorously validated at the controller level using `jakarta.validation` annotations to ensure data integrity before reaching the domain layer.

## Performance
* **Database Aggregations:** Reports (e.g., Top Busiest Airports, Route Utilization) and global metrics (e.g., Total Network Distance) are calculated using optimized JPQL projections at the database level to minimize memory consumption and maximize response times.

## Supportability
* **Extensibility (Strategy Pattern):** Algorithms for complex operations (like searching for alternative routes) are implemented using the Strategy Design Pattern, adhering to the Open/Closed Principle. New routing algorithms (e.g., Eco-Friendly vs. Fewest Stops) can be injected without modifying core services.
* **Testability:** The system architecture is fully decoupled, allowing for extensive unit testing. The business and service layers maintain near 100% test coverage (verified via JaCoCo).

## + Design Constraints
* **Architecture:** The application is built using a layered architecture adhering to Domain-Driven Design (DDD) principles.
* **Framework:** Spring Boot (Java).
* **Database:** Relational Database Management System (H2 for development/testing, PostgreSQL for production), accessed via Spring Data JPA.