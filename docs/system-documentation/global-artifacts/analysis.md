# Analysis - AISafe Flight Management System

## Domain Rules & Constraints
 
To ensure data consistency and reflect real-world aviation operations, the following rules apply to the domain model:
 
1. **Location Validity**: A `Flight Route` can only be established if both the origin and destination `Airports` are registered in the system and have an `OPERATIONAL` status.
2. **Aircraft Suitability**: An `Aircraft` can only be assigned to a `Flight Route` if it satisfies **both** of the route's minimum technical requirements defined in `RouteRequirement`:
   - Range: the aircraft model's maximum range must be greater than or equal to the route's minimum range requirement (`maxRange >= minRangeRequired`).
   - Capacity: the aircraft's active seat configuration capacity must be greater than or equal to the route's minimum capacity requirement (`activeCapacity >= minCapacityRequired`).
3. **Operational Readiness**: An `Aircraft` with status `UNDER_MAINTENANCE` cannot be assigned to any `Scheduled Flight`. The aircraft only becomes eligible for assignment once its status returns to `AVAILABLE`.
4. **Airport Certification**: An `Aircraft` can only be assigned to a `Flight Route` if the origin and destination `Airports` both hold a valid `AirplaneCertification` for that aircraft's model, confirming the model is authorized to operate at those facilities.
5. **Aircraft Scheduling & Turnaround Time**: An `Aircraft` cannot be double-booked. To ensure realistic ground operations, consecutive `Scheduled Flights` for the same aircraft must respect a mandatory turnaround buffer (e.g., 30 minutes) between the arrival of the previous flight and the departure of the next.
6. **Unique Identification**: The following fields must be unique across the system:
   - `Aircraft.registrationNumber` — no two aircraft can share the same registration number.
   - `Airport.iataCode` — no two airports can share the same IATA code.
   - `FlightRoute.routeId.id` — no two routes can share the same route identifier.
   - `User.username` — no two users can share the same username.
7. **Concurrency Control (Optimistic Locking)**: To prevent lost updates, `Aircraft`, `FlightRoute`, and `ScheduledFlight` aggregate roots include a `version` field (type `Long`). Any concurrent attempt to update their state (e.g., changing aircraft status, updating route details) validates this version to prevent data inconsistency.
8. **Concurrency Control (Pessimistic Locking)**: To strictly prevent race conditions during the critical process of scheduling flights (e.g., two ATCCs trying to book the exact same aircraft simultaneously), the system employs pessimistic write locks at the database level when querying for aircraft availability.
9. **Data Immutability & Audit Trails**: Critical operational data is never physically deleted from the database. A `FlightRoute` is marked as `DEACTIVATED` instead of deleted, and a `ScheduledFlight` is marked as `CANCELED`, preserving the referential integrity and operational history.