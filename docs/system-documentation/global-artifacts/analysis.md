# Analysis - AISafe Flight Management System
## Domain Rules & Constraints
 
To ensure data consistency, the following rules apply to the domain model:
 
1. **Location Validity**: A `Flight Route` can only be established if both the origin and destination `Airports` are registered in the system and have an `OPERATIONAL` status.
2. **Aircraft Suitability**: An `Aircraft` can only be assigned to a `Flight Route` if it satisfies **both** of the route's minimum technical requirements defined in `RouteRequirement`:
   - Range: the aircraft model's maximum range must be greater than or equal to the route's minimum range requirement (`maxRange >= minRangeRequired`).
   - Capacity: the aircraft's active seat configuration capacity must be greater than or equal to the route's minimum capacity requirement (`activeCapacity >= minCapacityRequired`).
3. **Operational Readiness**: An `Aircraft` with status `UNDER_MAINTENANCE` cannot be assigned to any `Scheduled Flight`. The aircraft only becomes eligible for assignment once its status returns to `AVAILABLE`.
4. **Airport Certification**: An `Aircraft` can only be assigned to a `Flight Route` if the origin and destination `Airports` both hold a valid `AirplaneCertification` for that aircraft's model, confirming the model is authorized to operate at those airports.
5. **Unique Identification**: The following fields must be unique across the system:
   - `Aircraft.registrationNumber` — no two aircraft can share the same registration number.
   - `Airport.iataCode` — no two airports can share the same IATA code.
   - `FlightRoute.routeId` — no two routes can share the same route identifier.
   - `User.username` — no two users can share the same username.
6. **Concurrent Access & Optimistic Locking**: The `Aircraft` aggregate includes a `version` field (type `Long`) to support optimistic locking. Any concurrent attempt to update an aircraft's status (e.g., marking it as `UNDER_MAINTENANCE` or `IN_FLIGHT`) must validate this version field to prevent race conditions and data inconsistency.