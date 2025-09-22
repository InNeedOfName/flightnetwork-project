package org.pi2.service.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.pi2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * API Endpoints for serving the solution of the tasks
 */
public class Endpoints{
    private static final Logger log = LoggerFactory.getLogger(Endpoints.class); // logger for easier debugging
    private final FlightNetwork flightNetwork; // Data structure
    private final API flightAPI; // Logic service layer/ implementation of the tasks

    /**
     * Constructor inits the endpoint controllers with the required dependencies
     * @param flightNetwork the Data structure containing the flights,routes and airports
     * @param flightAPI Service layer
     */
    public Endpoints(FlightNetwork flightNetwork, API flightAPI) {
        this.flightNetwork = flightNetwork;
        this.flightAPI = flightAPI;
    }

    /**
     * Sets up the routes for the endpoints
     * @param app the javalin instance used
     */
    public void setupRoutes(Javalin app) {
        // Welcome endpoint, contains a list of endpoints and a welcome message
        app.get("/", this::welcome);

        // Flight API endpoints
        app.get("/task/hasDirectRoute/{from}/{to}", this::hasDirectRoute); // task 1
        app.get("/task/getFlight/{routeOrigin}/{routeDestination}", this::getFlight); // Task 2
        app.get("/task/planTripRoute/{from}/{to}", this::planTripRoute); // Task 3 without criteria
        app.get("/task/planTripFlight/{from}/{to}/{criteria}", this::planTripFlight); // Task 3 with criteria
        app.get("/stats",this::stats); // Small stats page with information about the database. I guess I just wanted to add something
        // Global exception handler for error responses
        app.exception(Exception.class, this::handleException);
    }

    /**
     * Returns the database stats
     * For further Information consult the readme.me

     * HTTP: GET /stats
     * Respnse: Json with count of airports,flight and routes
     */
    private void stats(Context ctx) throws SQLException {
        ctx.status(200);
        Integer totalRoutes= flightNetwork.getTotalRoutes();
        Integer totalAirports=flightNetwork.getTotalAirports();
        Integer totalFlights=flightNetwork.getTotalFlights();

        Map<String,Object> response=Map.of(
                "amount of Airports in Db",totalAirports,
                "Amount of Flights in Database",totalFlights,
                "Amount of Routes in Database",totalRoutes);
        ctx.json(response);
    }

    /**
     * Returns welcome message, short statement, as well as a list of the endpoints in the API
     * For further Information consult the readme.me
     *
     * GET /
     * Respnse: JSON with endpoints
     */
    private void welcome(Context ctx) {
        ctx.status(200);
        Map<String, String> response = Map.of(
                "Welcome Message", "Welcome to the Flight API",
                "Statement", "For questions about the functionality, consult the readme.md",
                "Task 1","Endpoint provided at /task/hasDirectRoute/{from}/{to}",
                "Task 2","Endpoint provided at /task/getFlight/{routeOrigin}/{routeDestination}",
                "Task 3.1","Endpoint provided at /task/planTripRoute/{from}/{to}",
                "Task 3.2","Endpoint provided at /task/planTripFlight/{from}/{to}/{criteria}",
                "Stats","Database stats provided at /stats"
        );
        ctx.json(response);
    }

    /**
     * Returns the result of the hasDirectRoute method
     * Checks if two Airports are connected with a direct flight
     * For further Information consult the readme.me
     *
     * HTTP: GET /task/hasDirectRoute/{from}/{to}
     * Response: Json with boolean
     */
    private void hasDirectRoute(Context ctx) {
        try {
            // Extract path params
            String from = ctx.pathParam("from");
            String to = ctx.pathParam("to");

            // Verifying the Input
            Airport fromAirport = flightNetwork.getAirport(from);
            Airport toAirport = flightNetwork.getAirport(to);
            // handling of invalid originating airports
            if (fromAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Origin Airport not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }
            // handling of invalid destination airport
            if (toAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Destination Airport not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }

            // using hasDirectRoute to create the response
            boolean response = flightAPI.hasDirectRoute(fromAirport, toAirport);
            // Structuring the response
            Map<String, Object> fullResponse = Map.of(
                    "Originating Airport", from,
                    "Destination Airport", to,
                    "hasDirectRoute", response
            );
            ctx.json(fullResponse);

        }
        // Handling of exception
        catch (Exception e) {
            log.error("Error in hasDirectRoute endpoint", e);
            ctx.status(500);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            );
            ctx.json(errorResponse);
        }
    }

    /**
     * Retrieves all flights operating on a specific route.
     * For further Information consult the readme.me
     *
     * HTTP: GET /task/getFlight/{routeOrigin}/{routeDestination}
     * Response: JSON with route information and available flights
     */
    private void getFlight(Context ctx) {
        try {
            // Extract params
            String routeOrigin = ctx.pathParam("routeOrigin");
            String routeDestination = ctx.pathParam("routeDestination");

            // Find the route in the datastructure
            Route route = flightNetwork.getRoute(routeOrigin, routeDestination);
            log.info("Route Origin: " + routeOrigin + " Route Destination: " + routeDestination);
            log.info(String.valueOf(route));
            // handle case of route not existing
            if (route == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route not found",
                        "origin", routeOrigin,
                        "destination", routeDestination
                );
                ctx.json(errorResponse);
                return;
            }
            // Using the getFlights method to retrieve the flights operating on this route
            List<Flight> response = flightAPI.getFlights(route);
            // Handle case where no flights operate on the route
            if (response == null || response.isEmpty()) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route not found",
                        "origin", routeOrigin,
                        "destination", routeDestination
                );
                ctx.json(errorResponse);
                return;
            }

            // Return succesful response
            ctx.status(200);
            Map<String, Object> fullResponse = Map.of(
                    "Given Route", route,
                    "Flights on route", response
            );
            ctx.json(fullResponse);

        }
        // Catching exeptions in case of server errors
        catch (Exception e) {
            log.error("Error in getFlight endpoint", e);
            ctx.status(500);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            );
            ctx.json(errorResponse);
        }
    }


    /**
     * Plans a route using the TripPlanner class
     * For further Information consult the readme.me or TripPlanner class
     *
     * HTTP: GET /task/planTripRoute/{routeOrigin}/{routeDestination}
     * Response: JSON with the planned route
     */
    private void planTripRoute(Context ctx) {
        try {
            // Extract path params
            String from = ctx.pathParam("from");
            String to = ctx.pathParam("to");

            // Ensure that the airports exist
            Airport fromAirport = flightNetwork.getAirport(from);
            Airport toAirport = flightNetwork.getAirport(to);

            // Handling of the case, if originating airport does not exist
            if (fromAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route Origin not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }
            // Handling of the case, if destination airport does not exist
            if (toAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route Destination not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }

            // Using the TripPlanner to plan the trip
            List<Route> response = flightAPI.planTrip(fromAirport, toAirport);

            // Handle case, where no appropiate route exists in the network
            if (response.isEmpty()) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "No Route found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }

            // Return the found path
            ctx.status(200);
            Map<String, Object> fullResponse = Map.of(
                    "Originating Airport", from,
                    "Destination Airport", to,
                    "Routes", response
            );
            ctx.json(fullResponse);

        }
        // Exception handling
        catch (Exception e) {
            log.error("Error in planTripRoute endpoint", e);
            ctx.status(500);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            );
            ctx.json(errorResponse);
        }
    }

    /**
     * Plans optimal route between 2 airports with criteria "ShortesT" for the shortest routes and criteria "cheapest for the lowest cost flights
     * For further Information consult the readme.me or TripPlanner class
     *
     * HTTP: GET /task/planTripFlight/{routeOrigin}/{routeDestination}/{criteria}
     * Response: JSON with the planned route
     */
    private void planTripFlight(Context ctx) {
        try {
            // Extract path params
            String from = ctx.pathParam("from");
            String to = ctx.pathParam("to");
            String criteria = ctx.pathParam("criteria");

            // Verifying if the airports exist
            Airport fromAirport = flightNetwork.getAirport(from);
            Airport toAirport = flightNetwork.getAirport(to);

            // Handle of the case, where originating airport does not exist
            if (fromAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route Origin not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }
            // Handle of the case, where destination airport does not exist
            if (toAirport == null) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route Destination not found",
                        "from", from,
                        "to", to
                );
                ctx.json(errorResponse);
                return;
            }
            // Handle of the case, where criteria was not used correctly
            if (criteria == null || criteria.isEmpty()) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Criteria not found",
                        "criteria", criteria
                );
                ctx.json(errorResponse);
                return;
            }

            // Using the method from TripPlanner to plan the trip
            List<Flight> response = flightAPI.planTrip(fromAirport, toAirport, criteria);

            // Handle the case, if no routes according to the params exist
            if (response == null || response.isEmpty()) {
                ctx.status(404);
                Map<String, Object> errorResponse = Map.of(
                        "error", "Route not found",
                        "from", from,
                        "to", to,
                        "criteria", criteria
                );
                ctx.json(errorResponse);
                return;
            }

            // Return succesful response
            ctx.status(200);
            Map<String, Object> fullResponse = Map.of(
                    "Originating Airport", from,
                    "Destination Airport", to,
                    "criteria", criteria,
                    "Routes", response
            );
            ctx.json(fullResponse);

        }
        // Handle exceptions
        catch (Exception e) {
            log.error("Error in planTripFlight endpoint", e);
            ctx.status(500);
            Map<String, Object> errorResponse = Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            );
            ctx.json(errorResponse);
        }
    }
    // Global exception handler
    private void handleException(Exception e, Context ctx) {
        log.error("Unexpected server error", e);
        ctx.status(500);
        Map<String, Object> errorResponse = Map.of(
                "error", "Unexpected server error",
                "message", e.getMessage()
        );
        ctx.json(errorResponse);
    }
}