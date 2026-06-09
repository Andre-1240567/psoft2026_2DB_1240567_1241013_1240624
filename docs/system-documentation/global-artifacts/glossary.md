# Glossary - AISafe Flight Management System
 
This document defines the core domain concepts for the AISafe system, focusing on Aircraft Management, Airports, and Flight Routes (Work Packages 1, 2, and 3), as well as initial Setup.
 
| Concept | Definition | Classification |
|:---|:---|:---|
| **Aircraft** | A specific physical instance of a plane, identified by a unique registration number. It points to an active seat configuration and is tracked via its operational status. | **Entity / Aggregate Root** |
| **Aircraft Feature** | Specific technical or comfort characteristics of a model (e.g., WiFi-enabled, engine type) that can be freely added to the system. | **Value Object** |
| **Aircraft Model** | Technical specifications for a type of airplane. Defines manufacturer, baseline capacity, range, cruising speed, and possible seat configurations. Includes an optional technical image. | **Entity / Aggregate Root** |
| **Aircraft Status** | The specific availability state of an aircraft. Possible values: `AVAILABLE`, `INACTIVE`, `UNDER_MAINTENANCE`, `IN_FLIGHT`. | **Enum** |
| **Airplane Certification** | Authorization record specifying that a particular Aircraft Model is technically cleared and authorized to operate at a specific Airport. | **Value Object** |
| **Airport** | A registered aviation facility. It is the central hub for operations, containing infrastructure details, contact info, operational hours, and an optional photo. | **Entity / Aggregate Root** |
| **Airport Status** | The operational availability of an airport facility. Possible values: `OPERATIONAL`, `CLOSED`, `UNDER_MAINTENANCE`. | **Enum** |
| **Contact** | Communication details for an airport, classified by a specific Contact Type and associated with a department. | **Value Object** |
| **Contact Type** | The categorization of a communication method. Possible values: `PHONE`, `EMAIL`, `FAX`. | **Enum** |
| **Facility Service** | Structured information about services available at an airport (e.g., lounges, parking, specialized passenger assistance). | **Value Object** |
| **Flight Route** | A fixed point-to-point connection between an origin and a destination airport, acting as the operational blueprint. Includes a fixed distance and estimated flight time. | **Entity / Aggregate Root** |
| **Flight Status** | The specific lifecycle stage of a scheduled flight. Possible values: `SCHEDULED`, `DELAYED`, `IN_FLIGHT`, `COMPLETED`, `CANCELED`. | **Enum** |
| **Gate** | A specific departure or arrival point located within an airport Terminal. | **Value Object** |
| **GPS Coordinates** | The precise geographical mapping (latitude and longitude) of a location, used for mapping and route distance validation. | **Value Object** |
| **IATA Code** | A unique three-letter identifier for an airport (e.g., LIS, OPO) following international aviation standards. | **Value Object** |
| **Location** | Geographical and political data of an airport including its Region, Country, and City. It encompasses the GPS Coordinates. | **Value Object** |
| **Manufacturer** | The company responsible for building an Aircraft Model. Possible values: `BOEING`, `AIRBUS`, `EMBRAER`, `COMAC`, `ATR`, `TEXTRON_AVIATION`, `GULFSTREAM_AEROSPACE`, `PILATUS_AIRCRAFT`, `CIRRUS_AIRCRAFT`, `DIAMOND_AIRCRAFT`. | **Enum** |
| **Operational Hours** | The specific timeframe (opening and closing times) during which an airport is operational for flight traffic. | **Value Object** |
| **Orientation** | The compass direction a runway points towards. Possible values: `N`, `S`, `E`, `W`, `NW`, `NE`, `SW`, `SE`. | **Enum** |
| **Role** | The access level and system permissions assigned to a User. Possible values: `ADMIN`, `BACKOFFICE_OPERATOR`, `ATCC`. | **Enum** |
| **Route History** | A chronological log of all modifications, updates, or historical states associated with a specific flight route. | **Value Object** |
| **RouteRequirement** | Minimum technical constraints (range and seating capacity) that an aircraft must satisfy to be assigned to a specific route. | **Value Object** |
| **Route Status** | The availability of a route for scheduling new flights. Possible values: `ACTIVE`, `DEACTIVATED`. | **Enum** |
| **Runway** | Technical data regarding a landing strip, including its name/designator, length (in meters), and compass orientation. | **Value Object** |
| **Scheduled Flight** | A specific planned execution of a Flight Route. Tracks scheduled vs. actual departure/arrival times and its current flight status. It is completely encapsulated within the Flight Operations Aggregate. | **Entity** |
| **Seat Configuration** | A specific arrangement of seats and capacity defined for an Aircraft Model. Different aircraft of the same model can adopt different configurations, resulting in different passenger capacities. | **Value Object** |
| **Terminal** | A major physical building or infrastructure area within an airport that houses gates and facilities. | **Entity** |
| **Timezone** | The time zone regulation applicable to an airport, expressed as a standard zone identifier (e.g., `Europe/Lisbon`) to correctly account for regional time rules and daylight saving time. | **Value Object** |
| **User** | A system credential record representing an employee authorized to use the AISafe application. Each User is assigned exactly one Role that determines their access permissions. | **Entity / Aggregate Root** |