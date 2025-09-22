package org.pi2.service.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pi2.model.Airport;
import org.pi2.model.Flight;
import org.pi2.model.Route;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("sqlite.tmpdir", tempDir.getAbsolutePath());
        databaseManager = new DatabaseManager();

        databaseManager.clearTableData();
    }




    @AfterEach
    void tearDown() throws SQLException {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    // Airport Tests
    @Test
    void testInsertAndGetAirport() throws SQLException {
        Airport airport = new Airport("John F. Kennedy International", "JFK", "New York", "USA");


        databaseManager.insertAirport(airport);
        Airport retrievedAirport = databaseManager.getAirport("JFK");


        assertNotNull(retrievedAirport);
        assertEquals("John F. Kennedy International", retrievedAirport.getName());
        assertEquals("JFK", retrievedAirport.getCode());
        assertEquals("New York", retrievedAirport.getCity());
        assertEquals("USA", retrievedAirport.getCountry());
    }

    @Test
    void testGetNonExistentAirport() throws SQLException {

        Airport airport = databaseManager.getAirport("NONEXISTENT");


        assertNull(airport);
    }

    @Test
    void testInsertDuplicateAirport() throws SQLException {
        Airport airport1 = new Airport("Airport 1", "TEST", "City 1", "Country 1");
        Airport airport2 = new Airport("Airport 1", "TEST", "City 1", "Country 1");

        databaseManager.insertAirport(airport1);

        assertThrows(SQLException.class, () -> databaseManager.insertAirport(airport2));
    }

    @Test
    void testGetAllAirports() throws SQLException {
        Airport airport1 = new Airport("Airport 1", "A1", "City 1", "Country 1");
        Airport airport2 = new Airport("Airport 2", "A2", "City 2", "Country 2");
        Airport airport3 = new Airport("Airport 3", "A3", "City 3", "Country 3");

        databaseManager.insertAirport(airport1);
        databaseManager.insertAirport(airport2);
        databaseManager.insertAirport(airport3);

        List<Airport> airports = databaseManager.getAllAirports();

        assertEquals(3, airports.size());
        assertTrue(airports.stream().anyMatch(a -> a.getCode().equals("A1")));
        assertTrue(airports.stream().anyMatch(a -> a.getCode().equals("A2")));
        assertTrue(airports.stream().anyMatch(a -> a.getCode().equals("A3")));
    }

    @Test
    void testGetAllAirportsEmpty() throws SQLException {

        List<Airport> airports = databaseManager.getAllAirports();

        assertTrue(airports.isEmpty());
    }

    @Test
    void testGetTotalAirports() throws SQLException {
        Airport airport1 = new Airport("Airport 1", "A1", "City 1", "Country 1");
        Airport airport2 = new Airport("Airport 2", "A2", "City 2", "Country 2");

        databaseManager.insertAirport(airport1);
        databaseManager.insertAirport(airport2);

        Integer totalAirports = databaseManager.getTotalAirports();

        assertEquals(2, totalAirports);
    }

    // Flight Tests
    @Test
    void testInsertAndGetFlights() throws SQLException {
        Flight flight1 = new Flight("JFK", "LAX", "American", 500);
        Flight flight2 = new Flight("JFK", "MIA", "Delta", 300);

        databaseManager.insertFlight(flight1);
        databaseManager.insertFlight(flight2);

        List<Flight> flights = databaseManager.getFlightsFrom("JFK");

        assertEquals(2, flights.size());
        assertTrue(flights.stream().anyMatch(f ->
                f.getDestinationCode().equals("LAX") &&
                        f.getAirline().equals("American") &&
                        f.getCostInEuros() == 500));
        assertTrue(flights.stream().anyMatch(f ->
                f.getDestinationCode().equals("MIA") &&
                        f.getAirline().equals("Delta") &&
                        f.getCostInEuros() == 300));
    }

    @Test
    void testGetFlightsFromNonExistentOrigin() throws SQLException {
        List<Flight> flights = databaseManager.getFlightsFrom("NONEXISTENT");

        assertTrue(flights.isEmpty());
    }

    @Test
    void testInsertMultipleFlightsSameRoute() throws SQLException {
        Flight flight1 = new Flight("JFK", "LAX", "American", 500);
        Flight flight2 = new Flight("JFK", "LAX", "Delta", 450);

        databaseManager.insertFlight(flight1);
        databaseManager.insertFlight(flight2);

        List<Flight> flights = databaseManager.getFlightsFrom("JFK");

        assertEquals(2, flights.size());
        assertTrue(flights.stream().anyMatch(f -> f.getAirline().equals("American")));
        assertTrue(flights.stream().anyMatch(f -> f.getAirline().equals("Delta")));
    }

    @Test
    void testGetTotalFlights() throws SQLException {
        Flight flight1 = new Flight("JFK", "LAX", "American ", 500);
        Flight flight2 = new Flight("LAX", "JFK", "Delta", 450);

        databaseManager.insertFlight(flight1);
        databaseManager.insertFlight(flight2);

        Integer totalFlights = databaseManager.getTotalFlights();

        assertEquals(2, totalFlights);
    }

    // Route Tests
    @Test
    void testInsertAndGetRoute() throws SQLException {
        Route route = new Route("JFK", "LAX", 3944);

        databaseManager.insertRoute(route);
        Route retrievedRoute = databaseManager.getRoute("JFK", "LAX");

        assertNotNull(retrievedRoute);
        assertEquals("JFK", retrievedRoute.getOriginCode());
        assertEquals("LAX", retrievedRoute.getDestinationCode());
        assertEquals(3944, retrievedRoute.getDistanceInKilometer());
    }

    @Test
    void testGetNonExistentRoute() throws SQLException {
        Route route = databaseManager.getRoute("NONEXISTENT1", "NONEXISTENT2");

        assertNull(route);
    }

    @Test
    void testInsertDuplicateRoute() throws SQLException {
        Route route1 = new Route("JFK", "LAX", 3944);
        Route route2 = new Route("JFK", "LAX", 4000);

        databaseManager.insertRoute(route1);
        databaseManager.insertRoute(route2);

        Route retrievedRoute = databaseManager.getRoute("JFK", "LAX");

        assertNotNull(retrievedRoute);
        assertEquals(3944, retrievedRoute.getDistanceInKilometer());
    }

    @Test
    void testGetRoutesFrom() throws SQLException {
        Route route1 = new Route("JFK", "LAX", 3944);
        Route route2 = new Route("JFK", "MIA", 1759);
        Route route3 = new Route("LAX", "JFK", 3944);

        databaseManager.insertRoute(route1);
        databaseManager.insertRoute(route2);
        databaseManager.insertRoute(route3);

        List<Route> routesFromJFK = databaseManager.getRoutesFrom("JFK");

        assertEquals(2, routesFromJFK.size());
        assertTrue(routesFromJFK.stream().anyMatch(r -> r.getDestinationCode().equals("LAX")));
        assertTrue(routesFromJFK.stream().anyMatch(r -> r.getDestinationCode().equals("MIA")));
        assertFalse(routesFromJFK.stream().anyMatch(r -> r.getOriginCode().equals("LAX")));
    }

    @Test
    void testGetRoutesFromNonExistentOrigin() throws SQLException {
        List<Route> routes = databaseManager.getRoutesFrom("NONEXISTENT");

        assertTrue(routes.isEmpty());
    }

    @Test
    void testGetTotalRoutes() throws SQLException {
        Route route1 = new Route("JFK", "LAX", 3944);
        Route route2 = new Route("LAX", "JFK", 3944);
        Route route3 = new Route("JFK", "MIA", 1759);

        databaseManager.insertRoute(route1);
        databaseManager.insertRoute(route2);
        databaseManager.insertRoute(route3);

        Integer totalRoutes = databaseManager.getTotalRoutes();

        assertEquals(3, totalRoutes);
    }

    @Test
    void testCompleteWorkflow() throws SQLException {
        Airport jfk = new Airport("John F. Kennedy International", "JFK", "New York", "USA");
        Airport lax = new Airport("Los Angeles International ", "LAX", "Los Angeles", "USA");

        Flight flight = new Flight("JFK", "LAX", "American Airlines", 500);
        Route route = new Route("JFK", "LAX", 3944);

        databaseManager.insertAirport(jfk);
        databaseManager.insertAirport(lax);
        databaseManager.insertFlight(flight);
        databaseManager.insertRoute(route);

        assertEquals(2, databaseManager.getTotalAirports());
        assertEquals(1, databaseManager.getTotalFlights());
        assertEquals(1, databaseManager.getTotalRoutes());

        Airport retrievedJFK = databaseManager.getAirport("JFK");
        assertNotNull(retrievedJFK);
        assertEquals("John F. Kennedy International", retrievedJFK.getName());

        List<Flight> flights = databaseManager.getFlightsFrom("JFK");
        assertEquals(1, flights.size());
        assertEquals("LAX", flights.getFirst().getDestinationCode());

        Route retrievedRoute = databaseManager.getRoute("JFK", "LAX");
        assertNotNull(retrievedRoute);
        assertEquals(3944, retrievedRoute.getDistanceInKilometer());
    }


    @Test
    void testNullHandling() throws SQLException {
        Airport airportWithNullName = new Airport(null, "NULL", "City", "Country");

        assertThrows(SQLException.class, () -> databaseManager.insertAirport(airportWithNullName));

        Airport retrieved = databaseManager.getAirport("NULL");
        assertNull(retrieved);
    }

    @Test
    void testEmptyStringHandling() throws SQLException {
        Airport airportWithEmptyStrings = new Airport("", "EMPTY", "", "");

        assertDoesNotThrow(() -> databaseManager.insertAirport(airportWithEmptyStrings));

        Airport retrieved = databaseManager.getAirport("EMPTY");
        assertNotNull(retrieved);
        assertEquals("", retrieved.getName());
        assertEquals("EMPTY", retrieved.getCode());
        assertEquals("", retrieved.getCity());
        assertEquals("", retrieved.getCountry());
    }

    @Test
    void testZeroCostFlight() throws SQLException {
        Flight freeFlight = new Flight("JFK", "LAX", "Some Airlines", 0);

        assertDoesNotThrow(() -> databaseManager.insertFlight(freeFlight));

        List<Flight> flights = databaseManager.getFlightsFrom("JFK");
        assertEquals(1, flights.size());
        assertEquals(0, flights.getFirst().getCostInEuros());
    }

    @Test
    void testZeroDistanceRoute() throws SQLException {
        Route zeroRoute = new Route("JFK", "JFK", 0);

        assertDoesNotThrow(() -> databaseManager.insertRoute(zeroRoute));

        Route retrieved = databaseManager.getRoute("JFK", "JFK");
        assertNotNull(retrieved);
        assertEquals(0, retrieved.getDistanceInKilometer());
    }
}