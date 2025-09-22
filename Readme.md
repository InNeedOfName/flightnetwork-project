# Small uni project

This is a small project which I did for the cass datastructures and algorithms.
This project is written in Java, uses maven for the building, uses SQLite for a small flight network database and javalin to create small api endpoints.

## Usage

To initialize the database, the flights, airports, and flight routes must be placed as `.csv` files in `/resources`.  
From there, the database will be created, initialized, and populated. The project comes with the provided `.csv` files, which will populate the database on the first start.

After starting the API, it is currently accessible via ````http://localhost:8080/````.

There, the endpoints can be accessed.

If the flight network changes or is expanded, the database file must be replaced.

### Endpoints
Here is a list of the implemented endpoints.

#### Welcome Message
- **Endpoint:** ```/ ```
- **Method:** GET
- **Description:** Welcome message, displays the endpoints of the API as well as a statement pointing to the `readme.md`
- **Parameter:** None
- **Response:** JSON format

#### Database Statistics
- **Endpoint:** ```/stats ```
- **Method:** GET
- **Description:** Returns the number of elements in the database
- **Parameter:** None
- **Response:** JSON format
    - ````Amount of Flights in Database```` (Integer) Number of flights in the database
    - ````Amount of Routes in Database```` (Integer) Number of routes in the database
    - ````Amount of Airports in Db```` (Integer) Number of airports in the database
    - ````status```` (Str) Status of the database

#### Direct Route Check
- **Endpoint:** ```/task/hasDirectRoute/{from}/{to} ```
- **Method:** GET
- **Description:** Checks if there is a direct flight between two airports
- **Parameter:**
    - ```{from}``` (Str) Code of the origin airport
    - ```{to}``` (Str) Code of the destination airport
- **Response:** JSON format
    - ```Originating Airport``` (Str) Code of the origin airport
    - ```Destination Airport``` (Str) Code of the destination airport
    - ```hasDirectRoute``` (Boolean) Indicates whether a direct flight exists between the origin and destination airports

#### Get Flights between Airports
- **Endpoint:** ```/task/getFlight/{routeOrigin}/{routeDestination} ```
- **Method:** GET
- **Description:** Returns flights between two airports
- **Parameter:**
    - ```{routeOrigin}``` (Str) Code of the route origin
    - ```{routeDestination}``` (Str) Code of the route destination
- **Response:** JSON format
    - ````Given Route````
        - ```OriginCode``` (Str) Code of the origin airport of the given route
        - ```DestinationCode``` (Str) Code of the destination airport of the given route
        - ```distanceInKilometer``` (Integer) Distance of the flight route in kilometers
    - ```Flights on route``` (List\<Routes>) Flights available on the route as a list of routes
        - ```originCode``` (Str) Code of the origin airport of this route
        - ```destinationCode``` (Str) Code of the destination airport of this route
        - ```costInEuros``` (Integer) Price of the flight in euros

#### Plan Trip (no criteria)
- **Endpoint:** ```/task/planTripRoute/{from}/{to}/ ```
- **Method:** GET
- **Description:** Returns the shortest distance route between two airports
- **Parameter:**
    - ```from```
    - ```to```
- **Response:** JSON format

#### Plan Trip (with criteria)
- **Endpoint:** ```/task/planTripFlight/{from}/{to}/{criteria} ```
- **Method:** GET
- **Description:** Returns a planned trip between two airports based on a given criterion
- **Parameter:**
    - ```from``` (Str) Code of the origin airport
    - ```to``` (Str) Code of the destination airport
    - ```criteria``` (Str) Criterion for the API call. The criteria ```cheapest``` and ```shortest``` are implemented
- **Response:** JSON format
    - ```criteria``` (Str) The criterion of the API call
    - ```Destination Airport``` (Str) Code of the destination airport
    - ```Originating Airport``` (Str) Code of the origin airport
    - ```Routes``` (List\<Routes>) List of flights required to travel from the origin airport to the destination airport
        - ```originCode``` (Str) Code of the origin airport for this part of the route
        - ```destinationCode``` (Str) Code of the destination airport for this part of the route
        - ```airline``` (Str) Airline for this part of the route
        - ```costInEuros``` (Integer) Cost of this part of the route in euros

### Example questions
The API is prepared for a few questions.
It can answer:
#### Is there a direct flight between airports?
Takes in 2 Airport codes, returns the boolean value of the answer
#### What Flights are on Route XYZ?
Takes in a Route, returns the flights which are on Route
#### Can you plan me a route between 2 airports?
Takes in origin and destination Airport and returns the route necessary for the trip
Uses Dijkstra's algorithm
#### Can you plan me the shortest/cheapest route between 2 airports?
Takes in origin and destination Airport and returns the route necessary for the trip with the possible keywords "cheapest" or "shortest".
Uses Dijkstra's algorithm with cost or distance as optimization Criteria

### Structure
```
|-/java
|--/org.pi2
|-Main.java                   // Main functionality, connects the overall flow
|---/model   <- Separation of data models from functionality
|----> Airport                // Implementation of the Airport object
|----> Flight                 // Implementation of the Flight object
|----> Route                  // Implementation of the Route object
|----> Flightnetwork          // Data structure to combine the elements of the network
|---/service <- Separation of services from the data models
|----/api                       
|------> API                  // Methods of the API / assignment
|------> Endpoints            // API endpoints
|----/data
|------> DatabaseManager      // Database operations / initialization
|------> CsvLoading           // Loading of CSV files
|----/planning
|------> TripPlannerRoute     // Algorithm for Dijkstra's algorithm without criteria
|------> TripPlannerFlight    // Algorithm for Dijkstra's algorithm with criteria
|-/resources 
|--> airports.csv             // Airport information for initialization
|--> flights.csv              // Flight information for initialization
|--> routes.csv               // Route information for initialization
```
