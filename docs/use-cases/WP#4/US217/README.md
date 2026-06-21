# US217 — Categorize Maintenance Records by Component

## 1. User Story

> As a Maintenance Technician, I want to categorize maintenance records by
> maintenance component (engine, airframe, avionics, interior, exterior).

## 2. Implementation Note

This user story does not introduce a new endpoint. Categorization is a
**structural property** of `MaintenanceRecord`, captured by the mandatory
`component: MaintenanceComponent` field — enforced at creation time in
**[US115B — Create Maintenance Record](../US115B/README.md)**.

```java
public enum MaintenanceComponent {
    ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR
}
```

Every `MaintenanceRecord` is categorized from the moment it is created
(`CreateMaintenanceRecordDTO.component` is `@NotBlank`), and the category is
exposed in every `MaintenanceRecordResponseDTO`.

## 3. Where Categorization Is Consumed

The component field becomes operationally useful through two other user stories:

- **[US218 — Search Maintenance Records](../US218/README.md)** — filter records
  by `component`, alongside aircraft and date range.
- **[US219 — View Ongoing Maintenance Activities](../US219/README.md)** — each
  ongoing record's `component` is visible in the response, letting a Maintenance
  Supervisor see at a glance what part of the fleet needs engine work vs.
  avionics work, etc.

## 4. Design Decision

### Categorization as a constructor-enforced invariant, not an optional tag
`component` is `@Enumerated(EnumType.STRING)` and `nullable = false` on the
entity, and validated as non-null in the `MaintenanceRecord` constructor.
Making it mandatory at creation — rather than an optional field set later —
guarantees that **every** record in the system is immediately categorizable,
so US218's filter and US219's fleet overview never have to handle
"uncategorized" records as an edge case.