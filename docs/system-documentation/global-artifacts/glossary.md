# Glossary - AISafe Flight Management System
 
This document defines the core domain concepts for the AISafe system, focusing on Aircraft Management, Airports, Flight Routes, and Maintenance Records (Work Packages 1, 2, 3, and 4), as well as initial Setup.
 
| Concept | Definition | Classification |
|:---|:---|:---|
| **Aircraft** | A specific physical instance of a plane, identified by a unique registration number. It maintains its manufacturing date, operational status, total flight hours, and number of assignments. | **Entity / Aggregate Root** |
| **Aircraft Model** | Technical specifications for a type of airplane. Defines manufacturer, seating capacity, fuel capacity, range, cruising speed, and an optional image. | **Entity / Aggregate Root** |
| **Aircraft Status** | The specific availability state of an aircraft. Possible values: `AVAILABLE`, `INACTIVE`, `UNDER_MAINTENANCE`, `IN_FLIGHT`. | **Enum** |
| **Airplane Certification** | Authorization record specifying that a particular Aircraft Model is technically cleared and authorized to operate at a specific Airport. | **Value Object** |
| **Airport** | A registered aviation facility. It is the central hub for operations, containing infrastructure details, contact info, operational hours, and an optional photo. | **Entity / Aggregate Root** |
| **ChecklistItem** | A specific step or action item within a maintenance template or record, tracking whether it has been completed. | **Value Object** |
| **Contact** | Communication details for an airport, classified by a specific Contact Type and associated with a department. | **Value Object** |
| **Contact Type** | The categorization of a communication method. Possible values: `PHONE`, `EMAIL`, `FAX`. | **Enum** |
| **Facility Service** | Structured information about services available at an airport (e.g., lounges, parking, specialized passenger assistance). | **Value Object** |
| **Flight Route** | A fixed point-to-point connection between an origin and a destination airport, acting as the operational blueprint. Includes a fixed distance and estimated flight time. | **Entity / Aggregate Root** |
| **Flight Status** | The specific lifecycle stage of a scheduled flight. Possible values: `SCHEDULED`, `COMPLETED`, `CANCELED`. | **Enum** |
| **Gate** | A specific departure or arrival point located within an airport Terminal. | **Value Object** |
| **GPS Coordinates** | The precise geographical mapping (latitude and longitude) of a location, used for mapping and route distance validation. | **Value Object** |
| **IATA Code** | A unique three-letter identifier for an airport (e.g., LIS, OPO) following international aviation standards. | **Value Object** |
| **Location** | Geographical and political data of an airport including its Region, Country, and City. It encompasses the GPS Coordinates. | **Value Object** |
| **MaintenanceComponent** | A categorization of aircraft parts affected by maintenance. Possible values: `ENGINE`, `AIRFRAME`, `AVIONICS`, `INTERIOR`, `EXTERIOR`. | **Enum** |
| **MaintenanceRecord** | A specific transactional record of a maintenance procedure performed on an aircraft. Tracks start date, duration, costs, completion notes, and its checklist progress. | **Entity / Aggregate Root** |
| **MaintenanceStatus** | The current state of a maintenance record. Possible values: `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELED`. | **Enum** |
| **MaintenanceTemplate** | A reusable catalog definition for maintenance procedures applicable to specific aircraft models. Includes a default duration and a standard checklist. | **Entity / Aggregate Root** |
| **Manufacturer** | The company responsible for building an Aircraft Model. Possible values: `BOEING`, `AIRBUS`, `EMBRAER`, `COMAC`, `ATR`, `TEXTRON_AVIATION`, `GULFSTREAM_AEROSPACE`, `PILATUS_AIRCRAFT`, `CIRRUS_AIRCRAFT`, `DIAMOND_AIRCRAFT`. | **Enum** |
| **Operational Hours** | The specific timeframe (opening and closing times) during which an airport is operational for flight traffic. | **Value Object** |
| **Orientation** | The compass direction a runway points towards. Possible values: `N`, `S`, `E`, `W`, `NW`, `NE`, `SW`, `SE`. | **Enum** |
| **Route History** | A chronological log of all modifications, updates, or historical states associated with a specific flight route. | **Value Object** |
| **Route Id** | A unique identifier for a Flight Route. | **Value Object** |
| **Route Requirement** | Minimum technical constraints (range and seating capacity) that an aircraft must satisfy to be assigned to a specific route. | **Value Object** |
| **Route Status** | The availability of a route for scheduling new flights. Possible values: `ACTIVE`, `DEACTIVATED`. | **Enum** |
| **Runway** | Technical data regarding a landing strip, including its name/designator, length (in meters), and compass orientation. | **Value Object** |
| **Scheduled Flight** | A specific planned execution of a Flight Route. Tracks scheduled vs. actual departure/arrival times and its current flight status. | **Entity / Aggregate Root** |
| **Status** | The operational availability of an airport facility. Possible values: `OPERATIONAL`, `CLOSED`, `UNDER_MAINTENANCE`. | **Enum** |
| **TemplateType** | The classification of a maintenance template. Possible values: `INSPECTION`, `SCHEDULED_MAINTENANCE`, `OVERHAUL`, `MODIFICATION`. | **Enum** |
| **Terminal** | A major physical building or infrastructure area within an airport that houses gates and facilities. | **Entity** |
| **Timezone** | The time zone regulation applicable to an airport, expressed as a standard zone identifier (e.g., `Europe/Lisbon`) to correctly account for regional time rules and daylight saving time. | **Value Object** |
| **User** | A system credential record representing an employee authorized to use the AISafe application. Includes a comma-separated list of roles determining their access permissions. | **Entity / Aggregate Root** |