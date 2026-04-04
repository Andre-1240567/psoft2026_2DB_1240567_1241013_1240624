# Glossary - AISafe Flight Management System

This document defines the core domain concepts for the AISafe system, focusing on Aircraft Management, Airports, and Flight Routes.

| Concept | Definition | Classification |
| :--- | :--- | :--- |
| **Aircraft Model** | Technical specifications for a type of airplane, including manufacturer, model name, seating capacity, fuel capacity, maximum range, and cruising speed. | **Entity / Aggregate Root** |
| **Aircraft** | A specific physical instance of a plane, identified by its unique registration number (tail number), manufacturing date, and current operational status. | **Entity** |
| **Airport** | A registered aviation facility identified by a unique IATA code, containing details such as name, city, country, timezone, coordinates, and runways. | **Entity / Aggregate Root** |
| **IATA Code** | A unique three-letter identifier for an airport (e.g., LIS, OPO), following international aviation standards. | **Value Object** |
| **Runway** | Technical data regarding a landing strip at an airport, including its designation/name, length (meters), and orientation. | **Value Object** |
| **Airplane Certification** | A record or rule specifying that a particular Aircraft Model is authorized to operate (take off and land) at a specific Airport. | **Value Object** |
| **Flight Route** | A planned connection between a registered origin airport and a destination airport, including estimated duration and technical requirements. | **Entity / Aggregate Root** |
| **Route Requirements** | The minimum technical constraints (e.g., minimum range and passenger capacity) that an aircraft must satisfy to be assigned to a specific route. | **Value Object** |
| **Operational Status** | The current availability or state of a resource (e.g., "Active", "Under Maintenance" for Aircraft; "Operational", "Closed" for Airports). | **Value Object** |
| **Route History** | A chronological log of all modifications, updates, or historical states associated with a specific flight route. | **Value Object** |

