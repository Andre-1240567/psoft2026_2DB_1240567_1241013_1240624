# Glossary - AISafe Flight Management System

This document defines the core domain concepts for the AISafe system, focusing on Aircraft Management, Airports, and Flight Routes (Work Packages 1, 2, and 3), as well as initial Setup.

| Concept | Definition | Classification |
|:---|:---|:---|
| **Aircraft** | A specific physical instance of a plane, identified by a unique registration number. It points to an active seat configuration and is tracked via its operational status. | **Entity / Aggregate Root** |
| **Aircraft Feature** | Specific technical or comfort characteristics of a model (e.g., WiFi-enabled, engine type) that can be freely added to the system. | **Value Object** |
| **Aircraft Model** | Technical specifications for a type of airplane. Defines manufacturer, baseline capacity, range, cruising speed, and possible seat configurations. Includes a technical image. | **Entity / Aggregate Root** |
| **Aircraft Status** | The specific availability state of an aircraft (e.g., Available, Inactive, Under Maintenance, In-Flight). | **Enum** |
| **Airplane Certification** | Authorization record specifying that a particular Aircraft Model is technically cleared and authorized to operate at a specific Airport. | **Value Object** |
| **Airport** | A registered aviation facility. It is the central hub for operations, containing infrastructure details, contact info, operational hours, and an optional photo. | **Entity / Aggregate Root** |
| **Airport Status** | The operational availability of an airport facility (e.g., Operational, Closed, Under Maintenance). | **Enum** |
| **Contact** | Communication details for an airport, classified by a specific Contact Type and associated with a department. | **Value Object** |
| **Contact Type** | The categorization of a communication method (e.g., Phone, Email, Fax). | **Enum** |
| **Facility Service** | Structured information about services available at an airport (e.g., lounges, parking, specialized passenger assistance). | **Value Object** |
| **Flight Route** | A fixed point-to-point connection between an origin and a destination airport, acting as the operational blueprint. Includes a fixed distance and estimated time. | **Entity / Aggregate Root** |
| **Flight Status** | The specific lifecycle stage of a scheduled flight (e.g., Scheduled, Delayed, In-Flight, Completed, Canceled). | **Enum** |
| **Gate** | A specific departure or arrival point located within an airport Terminal. | **Entity** |
| **GPS Coordinates** | The precise geographical mapping (latitude and longitude) of a location, used for mapping and route distance validation. | **Value Object** |
| **IATA Code** | A unique three-letter identifier for an airport (e.g., LIS, OPO) following international aviation standards. | **Value Object** |
| **Location** | Geographical and political data of an airport including its Region, Country, and City. It encompasses the GPS Coordinates. | **Value Object** |
| **Operational Hours** | The specific timeframe (opening and closing times) during which an airport is operational for flight traffic. | **Value Object** |
| **Role** | The access level and system permissions assigned to a User (e.g., Admin, Backoffice Operator, ATCC). | **Enum** |
| **Route History** | A chronological log of all modifications, updates, or historical states associated with a specific flight route. | **Value Object** |
| **Route Requirements** | Minimum technical constraints (range and seating capacity) that an aircraft must satisfy to be assigned to a specific route. | **Value Object** |
| **Route Status** | The availability of a route for scheduling new flights (e.g., Active, Deactivated). | **Enum** |
| **Runway** | Technical data regarding a landing strip, including its name/designator, length (meters), and orientation. | **Entity** |
| **Scheduled Flight** | A specific planned execution of a Flight Route. Tracks scheduled vs. actual departure/arrival times. It is completely encapsulated within the Flight Operations Aggregate. | **Entity** |
| **Seat Configuration** | A specific arrangement of seats and capacity defined by the manufacturer. Different aircraft of the same model can adopt different configurations. | **Value Object** |
| **Terminal** | A major physical building or infrastructure area within an airport that houses gates and facilities. | **Entity** |
| **User** | A system credential record representing an employee (Administrator, Backoffice, or ATCC) authorized to use the AISafe application. | **Entity / Aggregate Root** |