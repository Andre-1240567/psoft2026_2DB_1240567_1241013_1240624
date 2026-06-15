# US209 - View Routes by Airport

## 1. Requirements

**US209:** As an ATCC, I want to view all routes that depart from or arrive at a specific airport.

**Acceptance Criteria:**
- The system must provide a list of routes where the specified airport is either the origin or the destination.
- The response must be paginated.
- The response must include HATEOAS links for navigation.
- Only users with the 'ATCC' role can access this information.

## 2. Analysis
This use case requires searching the `FlightRoute` aggregate by the IATA code of either the origin or destination airport. Since airports are part of the `Airport Management` subdomain and routes are part of the `Flight Routes` subdomain, the implementation involves cross-subdomain interaction. The endpoint is placed in `AirportController` (Airport subdomain) but delegates the search logic to `FlightRouteService` (Flight Routes subdomain) to maintain clean boundaries.

## 3. Design
The `AirportController` exposes the `GET /api/airports/{iataCode}/routes` endpoint. 
It uses `FlightRouteService.getRoutesByAirport(iataCode, pageable)` to fetch the data.
The service verifies the airport's existence using `AirportService` and then queries the `FlightRouteRepository` using a derived query method `findByOrigin_IataCode_CodeOrDestination_IataCode_Code`.

## 4. Tests
The endpoint is tested via Postman API requests (included in the WP#2B Postman collection), validating pagination, role-based access, and the correctness of the returned routes (both departing and arriving).
