package org.pi2.model;

import org.pi2.service.data.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the structure as well as provides a Data Access Layer for acessing the database
 */
public class FlightNetwork {

    private static final Logger log = LoggerFactory.getLogger(FlightNetwork.class);
    //Data structure for the flights,routes and airports
    private final Map<String, Airport> airports = new HashMap<>(); // Node storage code to Airport
    private final Map<String, List<Flight>> adjacencyList = new HashMap<>(); // adjascency List code to outgoing flight
    private final Map<String,List<Route>> routes = new HashMap<>(); // route storage code to outgoing route

    // Database connection
    public DatabaseManager databaseManager;


    /**
     * Constructor initalizes the flightnetwork
     * loads data form the SQLite database
     * @throws SQLException if db ooperation fails
     */
    public FlightNetwork() throws SQLException {
            this.databaseManager = new DatabaseManager();
            loadFromDatabase();
    }

    /**
     * Method to load Data from the database into the flightnetwork
     * We load all Airports from the database and load outgoing flights and routes for each airport
     *
     * @throws SQLException If db operations fail
     */
    public void loadFromDatabase() throws SQLException{
        // load airports
        List<Airport> dbAirports = databaseManager.getAllAirports();
        for (Airport airport: dbAirports) {
            // store airport
            airports.put(airport.getCode(),airport);
            // init empty adjacency list for the airport
            adjacencyList.putIfAbsent(airport.getCode(),new ArrayList<>());
            // load the outgoing flights
            List<Flight> flights = databaseManager.getFlightsFrom(airport.getCode());
            adjacencyList.put((airport.getCode()),flights);
            // load the outgoing routes
            List<Route> airportRoutes =databaseManager.getRoutesFrom(airport.getCode());
            routes.put(airport.getCode(), airportRoutes);
        }
    }

    /**
     * Adding a new Airport to network and database
     * @param airport The airport to be added
     */
    public void addAirport(Airport airport) {

            try{
                databaseManager.insertAirport(airport);}catch(SQLException e){
                log.info(e.getMessage());
            }

    }

    /**
     * Adding a new Route to network and db
     * @param route Route to be added
     */
    public void addRoute(Route route) {

        try{databaseManager.insertRoute(route);}catch(SQLException e) {log.info(e.getMessage());
}}

    /**
     * Adding a new flight to network and db
     * @param flight The flight object to be added to database
     */
    public void addFlight(Flight flight) {
        try{databaseManager.insertFlight(flight);}catch(SQLException e) {log.info(e.getMessage());
        }}

    /**
     * Get th
     * @param code The IATA Airportcode
     * @return routes originating at the airport
     */
    public List<Route> getRoutesFrom(Airport code) {
        return routes.getOrDefault(code, new ArrayList<>());
    }

    public List<Flight> getFlightsFrom(String code) throws SQLException {
        return databaseManager.getFlightsFrom(code);

        }
    public Airport getAirport(String code) throws SQLException {
        return databaseManager.getAirport(code);
    }

    public List<Flight> getFlights(Route route) throws SQLException {
        return databaseManager.getFlights(route);
    }
    /**
     * methhod to get a list of all Airports in the database
     * @return List of all airports in the database
     * @throws SQLException if db operations fail
     */
    public List<Airport> getAllAirports() throws SQLException {
        return databaseManager.getAllAirports();
    }

    /**
     * Get all outgoing routes from a given Airport
     * @param airportCode IATA COde of the Airport
     * @return List of outgoing routes
     */
    public List<Route> getRoutesFrom(String airportCode) throws SQLException {
        try {
            return databaseManager.getRoutesFrom(airportCode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes an originating airport and destination airport and returns the corresponding direct route, if it exists
     * @param routeOrigin IATA Code of the originating Airport
     * @param routeDestination IATA Code of the destination Airport
     * @return Route if one exists, else null
     */
    public Route getRoute(String routeOrigin, String routeDestination) throws SQLException {
        List<Route> routesFromOrigin = this.getRoutesFrom(routeOrigin);
        for (Route route: routesFromOrigin){
            if (route.getDestinationCode().equals(routeDestination)){
                return route;
            }
        }
        return null;
    }

    /**
     * Get total amount of Routes in the database for stats
     * @return Integer of the amount of routes in the database
     * @throws SQLException if query fails
     */
    public Integer getTotalRoutes() throws SQLException {
        return databaseManager.getTotalRoutes();
    }

    /**
     * Get total amount of airports in the database for stats
     * @return Integer of the amount of airports in the database
     * @throws SQLException if query fails
     */
    public Integer getTotalAirports() throws SQLException{
        return databaseManager.getTotalAirports();
    }

    /**
     * Database query to get the boolean value from the database for Task 1
     *
     * @param from originating airport
     * @param to destination airport
     * @return True is Route has direct flight
     * @throws SQLException if dp operations fail
     */
    public boolean hasDirectFlight(String from, String to) throws SQLException {
        return databaseManager.hasDirectFlight(from,to);
    }

    /**
     * Get total amount of Flights in the database for stats
     * @return Integer of the amount of flights in the database
     * @throws SQLException if query fails
     */
    public Integer getTotalFlights() throws SQLException{
        return databaseManager.getTotalFlights();
    }

    /**
     * Closes the database connection
     */
    public void close(){
        if (databaseManager != null) {
            try{
                databaseManager.close();}catch(SQLException e){
                log.info(e.getMessage());
            }
        }
    }


}