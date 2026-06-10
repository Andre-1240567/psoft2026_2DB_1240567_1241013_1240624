# US207 - Register Airport with Enhanced Features

## 1. Requirements

**US207:** As a Backoffice Operator, I want to register an airport with optional photos and detailed facilities information (terminals, gates, services).

**Acceptance Criteria:**
- The system must support the inclusion of optional photos (URLs) during airport registration.
- The system must allow specifying one or more terminals.
- Each terminal can have a list of gates and a list of services (e.g., Lounge, Retail) with their types and descriptions.

## 2. Analysis
This use case builds upon the foundation of US106 (Create Airport). Instead of just basic details and runways, the creation process is expanded to handle deeper facility aggregation. The `Airport` aggregate now includes a collection of `photos` and a collection of `Terminal` entities. Each `Terminal` acts as a local entity within the Airport aggregate containing collections of `Gate` and `FacilityService` value objects.

## 3. Design
The `AirportController` continues to use the `POST /api/airports` endpoint but expects the `CreateAirportRequestDTO` to be populated with the new enhanced fields (`photos` and `terminals`).
The `AirportService.createAirport()` method processes the incoming DTO. For the terminals list, it instantiates `Terminal` objects, populates them with their respective `Gate` and `FacilityService` elements, and attaches them to the `Airport` before saving it to the `AirportRepository`.

## 4. Tests
The endpoint is tested via Postman API requests (included in the WP#2B Postman collection), which validates that the created airport correctly returns the nested terminals, gates, and services.