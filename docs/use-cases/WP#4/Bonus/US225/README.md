# US225 — Bulk Import Airports from CSV

## 1. User Story

> As a **Backoffice Operator**, I want to import bulk airport data from CSV files.

## 2. Acceptance Criteria

- Only `BACKOFFICE_OPERATOR` may call this endpoint (JWT required).
- The uploaded file must be a non-empty CSV with the expected header row.
- Each row is validated with the same rules as US106 (`POST /api/airports`).
- A failing row does **not** block the remaining rows from being imported.
- The response always reports which rows were created and which failed, with a reason per failure.
- If no row could be imported → `400 Bad Request`.
- If at least one row was imported → `201 Created`.

## 3. CSV Format

**Header (case-sensitive, order-flexible):**
```
iataCode,name,region,city,country,timezone,latitude,longitude,runwayName,runwayLength,runwayOrientation
```

| Column | Mandatory | Notes |
|--------|:---------:|-------|
| `iataCode` | ✅ | Exactly 3 uppercase letters |
| `name` | ✅ | Non-blank |
| `region` | ✅ | Non-blank |
| `city` | ✅ | Non-blank |
| `country` | ✅ | Non-blank |
| `timezone` | ✅ | Format: `UTC+01:00` or `UTC-05:00` |
| `latitude` | ❌ | Leave empty to omit GPS coordinates |
| `longitude` | ❌ | Leave empty to omit GPS coordinates |
| `runwayName` | ✅ | Non-blank |
| `runwayLength` | ✅ | Positive number (metres) |
| `runwayOrientation` | ✅ | One of: `N S E W NW NE SW SE` (case-insensitive) |

**Example:**
```csv
iataCode,name,region,city,country,timezone,latitude,longitude,runwayName,runwayLength,runwayOrientation
LHR,Heathrow Airport,Europe,London,United Kingdom,UTC+01:00,51.4700,-0.4543,09L,3902.0,E
OPO,Francisco Sa Carneiro,Europe,Porto,Portugal,UTC+01:00,,,36,2400.0,N
```

> One runway per row. Additional runways and facility details (terminals, contacts, photos) can be added after import via the dedicated endpoints (US106, US208).

## 4. Design Decisions

### Reuse of US106 pipeline
Each row is mapped into a `CreateAirportRequestDTO` and validated with the same Bean Validation constraints that guard `POST /api/airports`. No business rule is duplicated or bypassed.

### Partial import — fail per row, not per file
A bad row is recorded in `errors` and processing continues. This avoids losing hundreds of valid airports because of a single typo. The response always shows exactly which rows failed and why.

### Layer separation via AirportImportResult
The service returns a plain `AirportImportResult` (domain objects, no HATEOAS). The controller assembles HATEOAS links via `AirportModelAssembler`, keeping the same separation of concerns used throughout the project.

## 5. Diagrams

### System Sequence Diagram
![US225 SSD](svg/US225-SSD.svg)

### Sequence Diagram
![US225 SD](svg/US225-SD.svg)

### Class Diagram
![US225 CD](svg/US225-CD.svg)