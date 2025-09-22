package org.pi2.service.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.pi2.model.Airport;
import org.pi2.model.Flight;
import org.pi2.model.Route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement the database params and access
 *
 * Database scheme:
 * - airports table: stores Airport data
 * - routes table: stores route data
 * - flights table: stores flight data
 */
public class DatabaseManager {
    // init the Params for the logger and database connection
    private static final String DB_URL = "jdbc:sqlite:flightnetwork.db";
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private Connection connection;

    /**
     * Constructor to establish database connection
     * @throws SQLException if database connection or table creation fails
     */
    public DatabaseManager() throws SQLException {
        initializeDatabase();
    }

    /**
     * Establishes database connection
     * Creates the tables for the database
     * @throws SQLException if connection to the database fails
     */
    private void initializeDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
    }

    // Create the table for the airports
    // Using the unique constraint so that every airport is unique
    // A city can have multiple airports but every airport has its own unique IATA Code
    private void createTables() throws SQLException {
        String createAirportsTable = """
            CREATE TABLE IF NOT EXISTS airports (
                name TEXT NOT NULL,
                code TEXT PRIMARY KEY,
                city TEXT,
                country TEXT,
                UNIQUE(name,code,city,country)
            )
        """;
        log.info("Created Airports table");

        // Creating table for the flights
        // Using the unique constraint because in the given data, every flight is unique
        String createFlightsTable = """
            CREATE TABLE IF NOT EXISTS flights (
                origin_code TEXT NOT NULL,
                destination_code TEXT NOT NULL,
                airline TEXT NOT NULL,
                cost_in_euroes INTEGER NOT NULL,
                FOREIGN KEY (origin_code) REFERENCES airports(code),
                FOREIGN KEY (destination_code) REFERENCES airports(code),
                UNIQUE(origin_code,destination_code,airline,cost_in_euroes)
            )
        """;
        log.info("Created Flights table");

        // Creating table for the routes
        // Using the unique constraint because in the given data, every route is unique
        String createRoutesTable = """
            CREATE TABLE IF NOT EXISTS routes (
                origin_code TEXT NOT NULL,
                destination_code TEXT NOT NULL,
                distance_in_kilometers INTEGER NOT NULL,
                FOREIGN KEY (origin_code) REFERENCES airports(code),
                FOREIGN KEY (destination_code) REFERENCES airports(code),
                UNIQUE(origin_code,destination_code,distance_in_kilometers)
            )
        """;
        log.info("Created Routes table");

        // Creating the tables
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createAirportsTable);
            stmt.execute(createFlightsTable);
            stmt.execute(createRoutesTable);
        }
        log.info("Tables created");
    }

    /**
     * Inserts Airport into the database
     * @param airport The Airport Object to be moved into the database
     * @throws SQLException if operation fails
     */
    public void insertAirport(Airport airport) throws SQLException {
        String sql = "INSERT INTO airports (name,code,city, country) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, airport.getName());
            pstmt.setString(2, airport.getCode());
            pstmt.setString(3, airport.getCity());
            pstmt.setString(4, airport.getCountry());
            pstmt.executeUpdate();
        }
        log.info("Airport inserted");
    }

    /**
     * Get the Airport from the database which corresponds to the input
     * @param code IATA Code of the Airport
     * @return Airport Object with the associated data
     * @throws SQLException if db operations fail
     */
    public Airport getAirport(String code) throws SQLException {
        String sql = "SELECT * FROM airports WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Airport(
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getString("city"),
                        rs.getString("country")
                );
            }
        }

        return null;
    }

    /**
     * Retrieve all airports from the database
     * @return List<Airport> List of all Airports in database
     * @throws SQLException if db operatoins fail
     */
    public List<Airport> getAllAirports() throws SQLException {
        List<Airport> airports = new ArrayList<>();
        String sql = "SELECT * FROM airports";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                airports.add(new Airport(
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getString("city"),
                        rs.getString("country")
                ));
            }
        }
        log.info("Returned all Airports");
        return airports;
    }

    /**
     * Inserts flight into database
     * @param flight Flight to be inserted
     * @throws SQLException if db operations fail
     */
    public void insertFlight(Flight flight) throws SQLException {
        String sql = "INSERT INTO flights (origin_code, destination_code, airline,cost_in_euroes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, flight.getOriginCode());
            pstmt.setString(2, flight.getDestinationCode());
            pstmt.setString(3, flight.getAirline());
            pstmt.setInt(4, flight.getCostInEuros());
            pstmt.executeUpdate();
        }
        log.info("Flight " +flight+" inserted");
    }

    /**
     * Method to get the flights originating from the originCode
     * @param originCode IATA Code of the originating airport
     * @return List<Flight> List of flights which match origin and destination
     * @throws SQLException if db operations fails
     */
    public List<Flight> getFlightsFrom(String originCode) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE origin_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, originCode);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                flights.add(new Flight(
                        rs.getString("origin_code"),
                        rs.getString("destination_code"),
                        rs.getString("airline"),
                        rs.getInt("cost_in_euroes")
                ));
            }
        }
        log.info("Returned flights from " + originCode);
        return flights;
    }

    /**
     * Inserts a route into the database
     * @param route Route to be inserted
     * @throws SQLException if db operations fail
     */
    public void insertRoute(Route route) throws SQLException {
        // Fixed: Removed airline column and fixed column name
        String sql = "INSERT INTO routes (origin_code, destination_code, distance_in_kilometers) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, route.getOriginCode());
            pstmt.setString(2, route.getDestinationCode());
            pstmt.setInt(3, route.getDistanceInKilometer());
            pstmt.executeUpdate();
        }
        log.info("Route " +route+" inserted");
    }

    /**
     * Get the routes originated from the originCode
     * @param originCode IATA Code for an airport
     * @return List<Route> A list of all routes originating at the given IATA Code
     * @throws SQLException If db operations fail
     */
    public List<Route> getRoutesFrom(String originCode) throws SQLException {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT * FROM routes WHERE origin_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, originCode);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                routes.add(new Route(
                        rs.getString("origin_code"),
                        rs.getString("destination_code"),
                        rs.getInt("distance_in_kilometers") // Fixed column name
                ));
            }
        }
        log.info("Returned routes from " + originCode);
        return routes;
    }

    /**
     * Retrieve Routes that correspond to the originCode and destinationCode
     * @param originCode IATA Code for the originating airport
     * @param destinationCode IATA Code for the destination Airport
     * @return Route which matches the origin and destination
     * @throws SQLException If db operations fail
     */
    public Route getRoute(String originCode, String destinationCode) throws SQLException {
        String sql = "SELECT * FROM routes WHERE origin_code = ? AND destination_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, originCode);
            pstmt.setString(2, destinationCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Route(
                        rs.getString("origin_code"),
                        rs.getString("destination_code"),
                        rs.getInt("distance_in_kilometers") // Fixed column name
                );
            }
        }
        return null;
    }

    /**
     * Retrieving the amount of Routes in the database
     * @return Integer Amount of Routes in database
     * @throws SQLException If db operations fail
     */
    public Integer getTotalRoutes()throws SQLException {
        String sql = "SELECT Count(*) FROM routes";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        log.info("Total routes returned");
        return null;
    }

    /**
     * Retrieving the amount of Flights in the database
     * @return Integer Amount of Flights in database
     * @throws SQLException If db operations fail
     */
    public Integer getTotalFlights()throws SQLException {
        String sql = "SELECT Count(*) FROM flights";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        log.info("Total flights returned");
        return null;
    }

    /**
     * Retrieving the amount of airports in the database
     * @return Integer Amount of Airports in database
     * @throws SQLException If db operations fail
     */
    public Integer getTotalAirports()throws SQLException {
        String sql = "SELECT Count(*) FROM Airports";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        log.info("Total Airports returned");
        return null;
    }

    /**
     * Checking if a route has a direct flight
     * A route has a direct flight if there exists at least one flight, that has the same origin and destination as the route
     * By Limiting
     *
     * @param to    Originating Airport IATA Code
     * @param from  Destination Airport IATA Code
     * @return true if route has a direct flight
     * @throws SQLException if db operations fail
     */
    public boolean hasDirectFlight(String to, String from) throws SQLException {
        String sql = "SELECT 1 FROM flights WHERE origin_code = ? AND destination_code" +
                " = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, to);
            pstmt.setString(2, from);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * SQL query to get all Flights which fly on the given route
     * Only direct flights on the route are considered
     *
     * @param route Route to be observed
     * @return List of direct flights on route
     * @throws SQLException if db operations fail
     */
    public List<Flight> getFlights(Route route) throws SQLException {
        List<Flight> matchingFlights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE origin_code = ? AND destination_code = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, route.getOriginCode());
            pstmt.setString(2, route.getDestinationCode());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Flight flight = createFlightFromResultSet(rs);
                    matchingFlights.add(flight);
                }
            }
        }
        return matchingFlights;
    }

    /**
     * Helper Method to create a flight for the method above
     * @param rs the Result of the query
     * @return Flight Object with the params of the query
     * @throws SQLException if db operations fail
     */
    private Flight createFlightFromResultSet(ResultSet rs) throws SQLException {

        String origin = rs.getString("origin_code");
        String destination = rs.getString("destination_code");
        String airline = rs.getString("airline");
        Integer CostInEuros = rs.getInt("cost_in_euroes");
        return new Flight(origin,destination,airline,CostInEuros);
    }
    /**
     * Closes the database connection
     * @throws SQLException If db operations fail
     */
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Clearin gup tables for testing
     * @throws SQLException if db operations fail
     */
    public void clearTableData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("DELETE FROM flights");
            stmt.execute("DELETE FROM routes");
            stmt.execute("DELETE FROM airports");

            log.info("All table data cleared");
        }
    }

}