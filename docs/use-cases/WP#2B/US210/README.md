# US210 - Busiest Airports Statistics

## 1. Requirements

**US210:** As a Backoffice Operator, I want to generate statistics on the busiest airports by number of routes.

**Acceptance Criteria:**
- The system must provide a list of airports sorted by the total number of routes (origin + destination).
- Only users with the 'BACKOFFICE_OPERATOR' role can access this information.
- The results should be provided via a REST API.

## 2. Analysis
This use case requires aggregating data from the `FlightRoute` table, specifically counting how many times each airport ID appears as an origin or a destination. To achieve optimal performance, a Native SQL query with `UNION ALL` is used to combine the counts from both columns and then group them by airport. The results are mapped to a specialized DTO (`BusiestAirportDTO`).

## 3. Design
The `AirportController` exposes the `GET /api/airports/statistics/busiest` endpoint. 
It uses `FlightRouteService.getBusiestAirports()` to fetch the statistical data.
The service calls the native query in `FlightRouteRepository` and maps the `Object[]` results to `BusiestAirportDTO` objects.

## 4. Tests
The endpoint is tested via Postman API requests (included in the WP#2B Postman collection), validating role-based access and the correctness of the aggregated counts and sorting.
