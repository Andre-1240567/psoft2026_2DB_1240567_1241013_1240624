# Analysis - AISafe Flight Management System
## Domain Rules & Constraints

To ensure data consistency, the following rules apply to the domain model:

1. **Location Validity**: A `Flight Route` can only be established if both the origin and destination `Airports` are registered in the system and have an "Operational" status.
2. **Aircraft Suitability**: An `Aircraft` can only fly a `Flight Route` if its `Aircraft Model` matches the route's technical requirements (Range >= Route Distance).
3. **Operational Readiness**: Aircraft marked as "Under Maintenance" are excluded from flight assignments until their status returns to "Active".
4. **Unique Identification**: Every `Aircraft` must have a unique Registration Number, and every `Airport` must have a unique IATA Code.