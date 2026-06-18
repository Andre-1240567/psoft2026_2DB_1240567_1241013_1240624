# WP#3B - Architecture

This folder contains the high-level architectural diagrams that model the actors, domain, and behavior for Work Package 3B of the AISafe system.

These diagrams provide a conceptual foundation for all the User Stories (both core and bonus) implemented in this phase.

## Diagrams Overview

### 1. Use Case Diagram (UCD)
Illustrates the interactions between the system's actors (e.g., `ATCC` and `BACKOFFICE_OPERATOR`) and the specific use cases developed in WP#3B (Flight Scheduling, Reporting, Exporting, etc.).
* **[View Use Case Diagram](svg/WP#3B-UCD.svg)**

### 2. Domain Model (DM)
Represents the structural model of the domain for this work package. It highlights the aggregates (`ScheduledFlight`), entities, value objects, and how they relate to the aggregates from previous work packages (like `Aircraft` and `Airport`).
* **[View Domain Model](svg/WP#3B-DM.svg)**

### 3. State Machine Diagram (SMD)
Models the dynamic states and transitions of key entities in the system. For instance, it tracks the lifecycle of a `ScheduledFlight` from its creation to states like `COMPLETED` or `CANCELLED`.
* **[View State Machine Diagram](svg/WP#3B-SMD.svg)**

---
> **Note:** The source code for all diagrams is written in PlantUML and can be found in the `puml/` directory. The rendered images are available in the `svg/` directory.