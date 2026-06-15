# US208 - Update Airport Details

## 1. Requirements

**US208:** As a Backoffice Operator, I want to update airport details including operational hours and contact information.

**Acceptance Criteria:**
- The system must allow updating the operational hours (opening and closing times).
- The system must allow adding/updating contact information (e.g., phone, email) with their respective departments.
- Proper input validation for time formats and contact details is required.

## 2. Analysis
This use case allows a Backoffice Operator to append or modify operational details of an already existing airport. The updates affect the `Airport` aggregate root, specifically its `operationalHours` (Value Object) and `contacts` (collection of Value Objects). Optimistic locking is not strictly required by the base criteria for this specific update unless we consider concurrent status changes, but standard entity updates apply.

## 3. Design
The `AirportController` exposes a `PATCH /api/airports/{iataCode}/details` endpoint. It receives an `UpdateAirportDetailsRequestDTO`.
The `AirportService` fetches the `Airport` by its IATA code, constructs the `OperationalHours` and `Contact` value objects from the DTO, and calls a domain method `updateDetails` on the `Airport` entity to apply the changes, before saving it via the repository.

## 4. Tests
Tested via `AirportServiceTest` (ensureUpdateAirportDetailsSuccess) and verified through Postman API requests.