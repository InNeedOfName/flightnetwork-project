package org.pi2.service.api;

import org.pi2.model.*;

import java.sql.SQLException;
import java.util.*;

import org.pi2.service.planning.TripPlannerFlight;
import org.pi2.service.planning.TripPlannerRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class API {

    /**
     * Setting up the logging,TripPlanners and FlightNetwork
     */
    private static final Logger log = LoggerFactory.getLogger(API.class);
    private final TripPlannerRoute tripPlannerRoute;
    private final TripPlannerFlight tripPlannerFlight;
    private final FlightNetwork network;

    public API(FlightNetwork network) {
        this.tripPlannerRoute = new TripPlannerRoute(network);
        this.tripPlannerFlight = new TripPlannerFlight(network);
        this.network = network;
    }


    /**
     * Implementation of Task 1
     * Returns a false/true Value if the Origin Airport and Destination Airport are connected by a direct flight.
     * The method takes in the Origin Airport and retrieves the Outgoing Flights from that Airport form the Database.
     * For each outgoing flight from the Origin Airport, the destination Code will be compared to each Destination of the outgoing flights.
     * If a match exists, the code returns true
     * If no match exists, the code returns false
     *
     * @param from Origin Airport Code
     * @param to Destination Airport Code
     * @return boolean if a direct flight between the two airpors exists or not
     * @throws SQLException if operation fails
     */
    public boolean hasDirectRoute(Airport from, Airport to) throws SQLException {
        return network.hasDirectFlight(from.getCode(),to.getCode());}


    /**
     * Implementation of Task 2
     * Returns a List of Flights that are on Route for the given Route.
     * Managed via SQL Query in the databaseManager
     * If a flight has the same origin and destination as the route, it is considered on route
     *
     *
     * @param route The flight Route that is being examined
     * @return List<Flights> The flights that are on the given route
     * @throws SQLException if db operaiton fails
     */
    public java.util.List<Flight> getFlights(Route route) throws SQLException {
        return network.getFlights(route);
    }

    /**
     * Implementation of Task 3
     * Plans a trip (may include connecting flights) between two airports. This only checks for routes and returns the shortest route without considering the flights.
     * The method is implemented in the service.TripPlannerRoute class. Uses Dijkstra's shortest path algorithm.
     * Consult the class for further comments.
     *
     * @param from Originating Airport
     * @param to Destination Airport
     * @return List<Route> A list of the Routes of the Trip
     */
    public java.util.List<Route> planTrip(Airport from, Airport to) throws SQLException {
        return this.tripPlannerRoute.findShortestPath(from, to);
    }

    /**
     * Implementation of Task 3
     * Plans a trip (may include connecting flights) between two airports. The criteria is either "shortest" or "cheapest". This considers the available flights.
     * The method is implemented in the service.TripPlannerFlight class. Uses Dijkstra's shortest path algorithm with different optimization criteria.
     * Consult the class for further comments.
     *
     * @param from Originating Airport
     * @param to Destination Airport
     * @return List<Flight> List of the Flights according to the Criteria
     */
    public java.util.List<Flight> planTrip(Airport from, Airport to, String criteria) throws SQLException {
        return this.tripPlannerFlight.planTrip(from, to, criteria);
    }


}
