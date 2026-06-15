# US211 - View Airports Grouped by Region or Country

## 1. Requirements

**US211:** As an ATCC, I want to view airports grouped by region or country.

**Acceptance Criteria:**
- The system must provide a list of airports grouped by the specified criteria (region or country).
- Only users with the 'ATCC' role can access this information.
- The response must be a Map where the keys are the regions/countries and the values are lists of airports.

## 2. Analysis
This use case requires fetching all airports from the `AirportRepository` and then using Java's `Collectors.groupingBy` to create a map based on the requested property (`region` or `country`) found within the `Location` value object.

## 3. Design
The `AirportController` exposes the `GET /api/airports/grouped?groupBy=region` (or `country`) endpoint.
It delegates the grouping logic to `AirportService.getAirportsGroupedBy(groupBy)`.
The service uses the `findAll()` method of the repository and performs in-memory grouping for simplicity and flexibility.

## 4. Tests
The endpoint is tested via Postman API requests (included in the WP#2B Postman collection), validating that the grouping correctly identifies the unique regions/countries and associated airports.
