package org.pi2.service.planning;

import org.pi2.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 /**
 * Realization of the Trip Planning, using Dijkstras Algorithm with multiple criteria
 *
 * Criteria
 * - "shortest" optimizes for minimum distance
 * - "cheapest" optimizes for minimum cost
 *
 * Algorithm modelling:
 * - Vertices: Airports
 * - Edges: Routes
 * - Weights: Distance of the routes
 *
 * Complexity:
 *  - Time complexity O(M+N)*log M)
 *      - with M=Amount of airport, N=Amount of routes
 *  - Space Complexity: O(M)
 */
public class TripPlannerFlight {

    /**
     * Flightnetwork contains all airports,routes and flights
     */
    private final FlightNetwork network;
    // Tripplanner that is being used, if Criteria is "shortest"
    private final TripPlannerRoute routePlanner;

    /**
     * Constructor initiliazes the flightNetwork and TripPlannerRoute
     * @param network the Flightnetwork on which we act upon
     */
    public TripPlannerFlight(FlightNetwork network) {
        this.network = network;
        this.routePlanner = new TripPlannerRoute(network);
    }

    /**
     * Plans a trip based on the specified criteria
     *
     * Selection:
     *  -"shortest" using existing planner
     *  -"cheapest" Using Dijkstra with cost as weight to find minimum cost path
     *
     * @param from Origin airport
     * @param to Destination airport
     * @param criteria Either "shortest" or "cheapest"
     * @return List of flights for the trip, empty list if no route found
     * @throws SQLException if db operations fail
     */
    public List<Flight> planTrip(Airport from, Airport to, String criteria) throws SQLException {
        // Validating Input
        if (from == null || to == null || criteria == null) {
            return Collections.emptyList();
        }

        // Switch cases to check the criteria and choose the proper Algorithm for the Solution.
        // if No criteria is choosen, nothing is returned
        switch (criteria.toLowerCase()) {
            case "shortest":
                return findShortestFlights(from, to);
            case "cheapest":
                return findCheapestFlights(from, to);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Finds flights that follow the shortest route path
     * @param from  Originating Airport
     * @param to    Destination Airport
     * @return List of flights following the criteria
     */
    private List<Flight> findShortestFlights(Airport from, Airport to) throws SQLException {
        // Get the shortest route path using the existing route planner
        List<Route> shortestRoutes = routePlanner.findShortestPath(from, to);

        if (shortestRoutes.isEmpty()) {
            return Collections.emptyList();
        }
        // Find flights that correspond to these routes
        return findFlightsForRoutes(shortestRoutes);
    }

    /**
     * Finds the cheapest flight path using Dijkstra's algorithm with cost as weight, optimizing for lowest total Trip cost
     * Similar to TripPlannerRoute, first wie initialize the data, make greedy choice, update neighbors and reconstruct optimal path
     *
     * @param from Originating Airport
     * @param to Destination Airport
     * @return List<Flight> List of the cheapest Flights
     * @throws SQLException if db Operations fail
     */
    private List<Flight> findCheapestFlights(Airport from, Airport to) throws SQLException {

        // Init the Datastructures for the algorithm
        Map<Airport, Integer> costs = new HashMap<>(); //Distance table, this Algorithm optimizes for cost
        Map<Airport, Flight> predecessors = new HashMap<>(); //Predecessor table so we can reconstruct Paths to make the optimal choice

        // Initialize airports and cost with maximum value
        for (Airport airport : network.getAllAirports()) {
            costs.put(airport, Integer.MAX_VALUE);
        }
        costs.put(from, 0); // Initializing our Starting Airport with cost 0 (freeby)

        // Creating a priority Queue, sorted by current minimum costs
        // The comparator is created after the costs are initialized
        PriorityQueue<Airport> priorityQueue = new PriorityQueue<>(
                Comparator.comparingInt(airport -> costs.getOrDefault(airport, Integer.MAX_VALUE))
        );
        priorityQueue.add(from);

        // The Main loop of the Algorithm
        while (!priorityQueue.isEmpty()) {
            // We take Airport with the minimum cost, the greedy choice
            Airport current = priorityQueue.poll();

            // If we reach the destination, the algorithm is stopped.
            if (current.equals(to)) {
                // if destination is reached, terminate early
                break;
            }

            // init out list of flights
            List<Flight> flights = null;
            // We get all flights from current airport straight from the database
            try {
                flights = network.getFlightsFrom(current.getCode());
            } catch (SQLException e) {
                throw new RuntimeException(e); // Exception handling
            }


            // Processing all outgoing flights from the airport

            for (Flight flight : flights) {
                Airport neighbor = network.getAirport(flight.getDestinationCode());
                if (neighbor == null) continue; // Skipping invalid destinations
                // init the Cost of the flight
                Integer flightCost = flight.getCostInEuros();
                if (flightCost == null) continue; // Skip the flight if cost is null
                /**
                 * Updating step
                 * Calcuclate new cost
                 */
                int newCost = costs.get(current) + flightCost;
                if (newCost < costs.getOrDefault(neighbor, Integer.MAX_VALUE)) { // Verifying if new path is cheaper than previous path
                    costs.put(neighbor, newCost);                                // Updating minimal cost
                    predecessors.put(neighbor, flight);                          // Storing the optimal flight to th eneighbor
                    priorityQueue.add(neighbor);                                 // Adding to queue for processing
                }
            }
        }
        // Reconstructing the optimal path for the trip

        return reconstructFlightPath(predecessors, from, to);
    }

    /**
     * Reconstruct flights by backtracking through predecessors
     * Takes the Params as a combination of the Airports and flights
     * Steps:
     *      1. Start at Destination
     *      2. Follow the predecessor chain backwards to the origin
     *      3. Collect the flights
     *      4. Use addFirst() to maintain the order
     *
     * @param predecessors Previous step flights
     * @param from Originating Airport
     * @param to Destination Airport
     * @return path the Reconstructed Path
     * @throws SQLException if dp operations fail
     */
    private List<Flight> reconstructFlightPath(Map<Airport, Flight> predecessors, Airport from, Airport to) throws SQLException {
        // Similar to tripPlannerRoute
        LinkedList<Flight> path = new LinkedList<>();
        Airport current = to;

        // Verify that we can reach the destination
        if (predecessors.get(current) == null && !from.equals(to)) {
            return Collections.emptyList();
        }

        //Back tracking from destination to Original origin(not for a single leg of the trip but entire trip) by using the predecessors
        while (predecessors.containsKey(current)) {
            Flight flight = predecessors.get(current);
            path.addFirst(flight);                                  // adding the flight at the begging of the list
            current = network.getAirport(flight.getOriginCode());  // Move to the origin(the previous Airport in the path)
        }
        return path;
    }

    /**
     * Converting a List of Routes into the flights
     * For each route, the method will select the cheapest available flight
     */
    private List<Flight> findFlightsForRoutes(List<Route> routes) {
        List<Flight> flights = new ArrayList<>();

        for (Route route : routes) {
            List<Flight> availableFlights = null;
            try {
                availableFlights = network.getFlightsFrom(route.getOriginCode());   // We are tasking all flights that are on Route
            } catch (SQLException e) {
                throw new RuntimeException(e);                                      //Exception handling
            }

            // We find the flight for the choosen route with the lowest cost
            Flight selectedFlight = null;
            int cheapestCost = Integer.MAX_VALUE;

            for (Flight flight : availableFlights) {
                if (flight.getDestinationCode().equals(route.getDestinationCode())){// Verifying that the flight is on Route i.e. has the correct Destination
                    Integer cost = flight.getCostInEuros();
                    if (cost != null && cost < cheapestCost) {                      // If the cost exists, i.e. the flight exists, and is lower than the cheapest Cost so far
                        cheapestCost = cost;                                        // Then we update costs
                        selectedFlight = flight;                                    // and flight
                    }
                }
            }
            if (selectedFlight != null) {                                           // If the flight exists, add it to the flights
                flights.add(selectedFlight);
            } else {
                return Collections.emptyList();                                     // If no flight found for this route, return empty list

            }
        }

        return flights;
    }


    /**
     * Formatting the Flight Trip
     * Couldnt include it in the service, had to focus on documentation
     */
    public String formatFlightTrip(List<Flight> flights) {
        if (flights.isEmpty()) {
            return "No flights found";
        }

        StringBuilder sb = new StringBuilder();
        int totalCost = 0;

        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            if (i > 0) sb.append(" → ");
            sb.append(String.format("%s (%s)",
                    flight.getOriginCode(),
                    flight.getAirline()));

            Integer cost = flight.getCostInEuros();
            if (cost != null) {
                totalCost += cost;
            }
        }

        // Add final destination
        if (!flights.isEmpty()) {
            sb.append(" → ").append(flights.get(flights.size() - 1).getDestinationCode());
        }

        sb.append(String.format(" (Total Cost: %d €)", totalCost));
        return sb.toString();
    }


    public String planAndFormatTrip(Airport from, Airport to, String criteria) throws SQLException {
        List<Flight> flights = planTrip(from, to, criteria);
        return formatFlightTrip(flights);
    }
}