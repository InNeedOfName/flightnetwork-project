package org.pi2.service.planning;

import org.pi2.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Realization of the Trip Planning, using Dijkstras Algorithm
 *
 * Algorithm modelling:
 * - Vertices: Airports
 * - Edges: Routes
 * - Weights: Distance of the routes
 *
 * Complexity_
 *  - Time complexity O(M+N)*log M)
 *      - with M=Amount of airport, N=Amount of routes
 *  - Space Complexity: O(M)
 */
public class TripPlannerRoute {
    /**
     * FlightNetwork contains all airports and routes
     * Declared as final so it wont change during construction
     */
    private final FlightNetwork network;

    /**
     * Constructor with dependency
     * @param network Flightnetwork on which we will act upon
     */
    public TripPlannerRoute(FlightNetwork network) {
        this.network = network;
    }

    /**
     * Finding the shortest path with Dijkstra
     * Algorithm Steps:
     *  1. Init
     *      Every Distance as max Value, set Starting Vertice to 0
     *  2. Greedy Choice
     *      Process Vertice with minimal known distance
     *  3. Relaxation
     *      Update neighbours, if better path was found
     *  4. Reconstruction
     *      We Reconstruct the optimal path by using a predecessor List
     * @param from Starting Airport
     * @param to Destination Airport
     * @return List of Routes for the shortest path, empty if no path exists
     */
    public List<Route> findShortestPath(Airport from, Airport to) throws SQLException {
        // Validation of Input
        if (from==null||to==null) return Collections.emptyList();

        /***
         * Step 1, Initialize Data
         * distance: Storing the minimal known distance
         * key: Destination airport
         * Value: Integer(Minimal distance in Km
         */
        Map<Airport, Integer> distances=new HashMap<>();
        /**
         * Predecessor table for path reconstruction
         * Key: Destinaiton Airport
         * Allows us to backtrack
         */
        Map<Airport, Route> predecessors=new HashMap<>();

        /**
         * priorityQueue: the minheap for greedily choosing the next vertice
         * sorting: Sorted by current distance, using distancess::get as comparator
         */
        PriorityQueue<Airport> priorityQueue=new PriorityQueue<>(Comparator.comparingInt(distances::get));

        /**
         * Initializing the distances
         */
        try {
            //marking all airports as unreachable
            for (Airport airport:network.getAllAirports()){
                distances.put(airport,Integer.MAX_VALUE);
            }
        } catch (SQLException e) {
            // Catching Exceptions in care or DB operation error
            throw new RuntimeException(e);
        }
        // Starting Airport has distance 0, our starting point
        distances.put(from,0);
        // Adding our starting point to the priorityQueue
        priorityQueue.add(from);

        /**
         * Main Part
         */
        while(!priorityQueue.isEmpty()){

            // Choose Airport with the lowest known distance
            Airport current = priorityQueue.poll();

            // Break if destination is reached
            // Avoid multiple useless iterations
            if (current.equals(to)) break;

            /**
             * Process all outgoing Routes from the Airport
             * Every route -> an edge in the network
             */
            for (Route route: network.getRoutesFrom(current.getCode())){
                Airport neighbor = null;
                try {
                    // Trying to find the Destination Airport of the route
                    neighbor = network.getAirport(route.getDestinationCode());
                } catch (SQLException e) {
                    throw new RuntimeException(e);  // SQL Error handling
                }
                if (neighbor==null) continue;       // Skip invalid routes

                /**
                 * Relaxation
                 *  Calculcate new distances via new distance= known distance+total distance
                 */
                int newDistance=distances.get(current)+route.getDistanceInKilometer();

                // Checking if the new path is actually shorter than the old one
                if (newDistance < distances.getOrDefault(neighbor,Integer.MAX_VALUE)){
                    distances.put(neighbor,newDistance);        // Update minimal known distance to neighbor
                    predecessors.put(neighbor,route);           // Store Route that might be more optimal
                    priorityQueue.add(neighbor);                // Adding to PriorityQueue
                }
            }
        }
        return reconstructPath(predecessors,from,to);
        }

    /**
     * Method to reconstruct the optimal path via Backtracking through the predecessors
     *
     * Algorithm:
     *      1. start with Destination Airport
     *      2. Follow Predecessor chain backwards to the origin
     *      3. Collect Routes in reverse order
     *      4. Using the addFirst() makes sure, that the order is correct
     *
     * @param predecessors  Map with optimal predecessor routes
     * @param from          Originating Airport
     * @param to            Destination Airport
     * @return              List of Routes
     */
    private List<Route> reconstructPath(Map<Airport, Route> predecessors, Airport from, Airport to) {
        /**
         * Using a Linked List für efficiency
         * addFirst() complexity: O(1), using ArrayList -> O(n)
         */
        LinkedList<Route> path = new LinkedList<>();
        // Starting the backtracking at Destination airport
        Airport current = to;
        // Verifying if we could reach a destination
        // if no predecessor exists for the destination -> No path
        // Exception: Origin = Destination (Empty Route)
        if (predecessors.get(current) == null && !from.equals(to)) {
            return Collections.emptyList();
        }

        /**
         * Following the predecessor Chain from End to start
         * As long as predecessors exist, this will continue
         * And terminate if the Origin Airport has no predecessor
         */
        while (predecessors.containsKey(current)) {
            // get Route with optimal path to current
            Route route = predecessors.get(current);
            // Add to start of List
            path.addFirst(route);
            // Move to Origin of current route
            try {
                current = network.getAirport(route.getOriginCode());
            } catch (SQLException e) {
                throw new RuntimeException(e);      // Exception handling
            }
        }
        // Path is now in the correct order( Destination -> Origin)
        return path;
    }

    /**
     * Formatting the Flight Trip
     * Couldnt include it in the service, had to focus on documentation
     */
    public String formatRoute(List<Route> routes) {
            if (routes.isEmpty()) {
                return "No route found";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < routes.size(); i++) {
                Route route = routes.get(i);
                if (i > 0) sb.append(" → ");
                sb.append(route.getOriginCode());
            }
            // Add The final destination
            if (!routes.isEmpty()) {
                sb.append(" to ").append(routes.get(routes.size() - 1).getDestinationCode());
            }

            int totalDistance = routes.stream().mapToInt(Route::getDistanceInKilometer).sum();
            sb.append(String.format(" (Total: %d km)", totalDistance));

            return sb.toString();
        }

        // Method to find and format trip in one call
        public String planAndFormatRoute(Airport from, Airport to) throws SQLException {
            List<Route> routes = findShortestPath(from, to);
            return formatRoute(routes);
        }
    }