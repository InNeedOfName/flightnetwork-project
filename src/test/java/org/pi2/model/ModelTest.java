package org.pi2.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.pi2.service.data.DatabaseManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Nested
    @DisplayName("Testing Airports")
    class AirportTest {

        private Airport airport;

        @BeforeEach
        void setUp() {
            airport = new Airport("London Heathrow", "LHR", "London", "United Kingdom");
        }

        @Test
        @DisplayName("Airport should initialize correctly")
        void testConstructorAndGetters() {
            assertEquals("London Heathrow", airport.getName());
            assertEquals("LHR", airport.getCode());
            assertEquals("London", airport.getCity());
            assertEquals("United Kingdom", airport.getCountry());
        }

        @Test
        @DisplayName("Airport Name should return correctly")
        void testGetName() {
            assertEquals("London Heathrow", airport.getName());
            assertNotEquals("LHR", airport.getName());
            assertNotEquals("London", airport.getName());
            assertNotEquals("United Kingdom", airport.getName());
        }

        @Test
        @DisplayName("Airport Code should return correctly")
        void testGetCode() {
            assertEquals("LHR", airport.getCode());
            assertNotEquals("London Heathrow", airport.getCode());
            assertNotEquals("London", airport.getCode());
            assertNotEquals("United Kingdom", airport.getCode());
        }

        @Test
        @DisplayName("Airport City should return correctly")
        void testGetCity() {
            assertEquals("London", airport.getCity());
            assertNotEquals("London Heathrow", airport.getCity());
            assertNotEquals("LHR", airport.getCity());
            assertNotEquals("United Kingdom", airport.getCity());
        }

        @Test
        @DisplayName("Airport Country should return correctly")
        void testGetCountry() {
            assertEquals("United Kingdom", airport.getCountry());
            assertNotEquals("London Heathrow", airport.getCountry());
            assertNotEquals("LHR", airport.getCountry());
            assertNotEquals("London", airport.getCountry());
        }

        @Test
        @DisplayName("Constructor should work with different Values")
        void testConstructorWithDifferentValues() {
            Airport munich = new Airport("Munich", "MUC", "Munich", "Germany");

            assertEquals("Munich", munich.getName());
            assertEquals("MUC", munich.getCode());
            assertEquals("Munich", munich.getCity());
            assertEquals("Germany", munich.getCountry());

            assertNotEquals(airport.getName(), munich.getName());
            assertNotEquals(airport.getCode(), munich.getCode());
            assertNotEquals(airport.getCountry(), munich.getCountry());
        }

        @Test
        @DisplayName("Null constructor should return null Airport")
        void testConstructorWithNullValues() {
            Airport nullAirport = new Airport(null, null, null, null);
            assertNull(nullAirport.getName());
            assertNull(nullAirport.getCode());
            assertNull(nullAirport.getCity());
            assertNull(nullAirport.getCountry());
        }

        @Test
        @DisplayName("Empty Airport should return empty")
        void testConstructorWithEmptyStrings() {
            Airport emptyAirport = new Airport("", "", "", "");

            assertEquals("", emptyAirport.getName());
            assertEquals("", emptyAirport.getCode());
            assertEquals("", emptyAirport.getCity());
            assertEquals("", emptyAirport.getCountry());

            assertNotNull(emptyAirport.getName());
            assertNotNull(emptyAirport.getCode());
            assertNotNull(emptyAirport.getCity());
            assertNotNull(emptyAirport.getCountry());
        }

        @Test
        @DisplayName("Test equals and hashCode if implemented")
        void testEqualsAndHashCode() {
            Airport airport2 = new Airport("London Heathrow", "LHR", "London", "United Kingdom");
            Airport differentAirport = new Airport("Munich", "MUC", "Munich", "Germany");

            assertEquals(airport, airport);

            // Test with different objects
            assertNotSame(airport, airport2);
            assertNotEquals(airport, differentAirport);
            assertNotEquals(airport, null);
            assertNotEquals(airport, "not an airport");
        }
    }

    @Nested
    @DisplayName("Testing Flights")
    class FlightsTest {
        private Flight flight;

        @BeforeEach
        void setUp() {
            flight = new Flight("LHR", "MUC", "British Airways", 140);
        }

        @Test
        @DisplayName("Constructor should create a non-null object")
        void constructor_shouldCreateNonNullObject() {
            assertNotNull(flight, "The flight object should not be null after creation.");
        }

        @Test
        @DisplayName("getOriginCode should return the correct origin code")
        void getOriginCode_shouldReturnCorrectValue() {
            assertEquals("LHR", flight.getOriginCode());
            assertNotEquals("MUC", flight.getOriginCode());
            assertNotEquals("British Airways", flight.getOriginCode());
            assertNotEquals("140", flight.getOriginCode());
        }

        @Test
        @DisplayName("getDestinationCode should return the correct destination code")
        void getDestinationCode_shouldReturnCorrectValue() {
            assertEquals("MUC", flight.getDestinationCode());
            assertNotEquals("LHR", flight.getDestinationCode());
            assertNotEquals("British Airways", flight.getDestinationCode());
            assertNotEquals("140", flight.getDestinationCode());
        }

        @Test
        @DisplayName("getAirline should return the correct airline")
        void getAirline_shouldReturnCorrectValue() {
            assertEquals("British Airways", flight.getAirline());
            assertNotEquals("LHR", flight.getAirline());
            assertNotEquals("MUC", flight.getAirline());
            assertNotEquals("140", flight.getAirline());
        }

        @Test
        @DisplayName("getCostInEuros should return the correct cost")
        void getCostInEuros_shouldReturnCorrectValue() {
            assertEquals(140, flight.getCostInEuros());
            assertNotEquals(139, flight.getCostInEuros());
            assertNotEquals(141, flight.getCostInEuros());
            assertNotEquals(0, flight.getCostInEuros());
            assertNotEquals(-140, flight.getCostInEuros());
        }

        @Test
        @DisplayName("toString should return a correctly formatted string")
        void toString_shouldReturnCorrectFormat() {
            String expectedString = "(LHR, MUC, British Airways, 140)";
            String actualString = flight.toString();
            assertEquals(expectedString, actualString);

            assertNotEquals("LHR", actualString);
            assertNotEquals("MUC", actualString);
            assertNotEquals("British Airways", actualString);
            assertNotEquals("140", actualString);

            assertTrue(actualString.contains("LHR"));
            assertTrue(actualString.contains("MUC"));
            assertTrue(actualString.contains("British Airways"));
            assertTrue(actualString.contains("140"));
        }

        @Test
        @DisplayName("Test flight with different values")
        void testFlightWithDifferentValues() {
            Flight flight2 = new Flight("JFK", "LAX", "American Airlines", 250);

            assertEquals("JFK", flight2.getOriginCode());
            assertEquals("LAX", flight2.getDestinationCode());
            assertEquals("American Airlines", flight2.getAirline());
            assertEquals(250, flight2.getCostInEuros());

            assertNotEquals(flight.getOriginCode(), flight2.getOriginCode());
            assertNotEquals(flight.getDestinationCode(), flight2.getDestinationCode());
            assertNotEquals(flight.getAirline(), flight2.getAirline());
            assertNotEquals(flight.getCostInEuros(), flight2.getCostInEuros());
        }

        @Test
        @DisplayName("Test flight with zero and negative costs")
        void testFlightWithEdgeCaseCosts() {
            Flight zeroCostFlight = new Flight("LHR", "MUC", "British Airways", 0);
            assertEquals(0, zeroCostFlight.getCostInEuros());

            Flight negativeCostFlight = new Flight("LHR", "MUC", "British Airways", -100);
            assertEquals(-100, negativeCostFlight.getCostInEuros());
        }

        @Test
        @DisplayName("Test flight with null and empty strings")
        void testFlightWithNullAndEmptyValues() {
            Flight nullFlight = new Flight(null, null, null, 100);
            assertNull(nullFlight.getOriginCode());
            assertNull(nullFlight.getDestinationCode());
            assertNull(nullFlight.getAirline());
            assertEquals(100, nullFlight.getCostInEuros());

            Flight emptyFlight = new Flight("", "", "", 50);
            assertEquals("", emptyFlight.getOriginCode());
            assertEquals("", emptyFlight.getDestinationCode());
            assertEquals("", emptyFlight.getAirline());
            assertEquals(50, emptyFlight.getCostInEuros());
        }
    }

    @Nested
    @DisplayName("Testing Routes")
    class RoutesTest {
        private Route route;

        @BeforeEach
        void setUp() {
            route = new Route("LHR", "MUC", 918);
        }

        @Test
        @DisplayName("Test that the Route object is not null")
        void testRouteObjectIsNotNull() {
            assertNotNull(route, "The Route object should not be null.");
        }

        @Test
        @DisplayName("Test getOriginCode returns the correct origin code")
        void testGetOriginCode() {
            assertEquals("LHR", route.getOriginCode());
            assertNotEquals("MUC", route.getOriginCode());
            assertNotEquals("918", route.getOriginCode());
        }

        @Test
        @DisplayName("Test getDestinationCode returns the correct destination code")
        void testGetDestinationCode() {
            assertEquals("MUC", route.getDestinationCode());
            assertNotEquals("LHR", route.getDestinationCode());
            assertNotEquals("918", route.getDestinationCode());
        }

        @Test
        @DisplayName("Test getDistanceInKilometer returns the correct distance")
        void testGetDistanceInKilometer() {
            assertEquals(918, route.getDistanceInKilometer());
            assertNotEquals(917, route.getDistanceInKilometer());
            assertNotEquals(919, route.getDistanceInKilometer());
            assertNotEquals(0, route.getDistanceInKilometer());
            assertNotEquals(-918, route.getDistanceInKilometer());
        }

        @Test
        @DisplayName("Test route with different values")
        void testRouteWithDifferentValues() {
            Route route2 = new Route("JFK", "LAX", 3983);

            assertEquals("JFK", route2.getOriginCode());
            assertEquals("LAX", route2.getDestinationCode());
            assertEquals(3983, route2.getDistanceInKilometer());

            assertNotEquals(route.getOriginCode(), route2.getOriginCode());
            assertNotEquals(route.getDestinationCode(), route2.getDestinationCode());
            assertNotEquals(route.getDistanceInKilometer(), route2.getDistanceInKilometer());
        }

        @Test
        @DisplayName("Test route with zero and negative distances")
        void testRouteWithEdgeCaseDistances() {
            Route zeroDistanceRoute = new Route("LHR", "LHR", 0);
            assertEquals(0, zeroDistanceRoute.getDistanceInKilometer());

            Route negativeDistanceRoute = new Route("LHR", "MUC", -100);
            assertEquals(-100, negativeDistanceRoute.getDistanceInKilometer());
        }

        @Test
        @DisplayName("Test route with null and empty strings")
        void testRouteWithNullAndEmptyValues() {
            Route nullRoute = new Route(null, null, 500);
            assertNull(nullRoute.getOriginCode());
            assertNull(nullRoute.getDestinationCode());
            assertEquals(500, nullRoute.getDistanceInKilometer());

            Route emptyRoute = new Route("", "", 750);
            assertEquals("", emptyRoute.getOriginCode());
            assertEquals("", emptyRoute.getDestinationCode());
            assertEquals(750, emptyRoute.getDistanceInKilometer());
        }
    }

    @Nested
    @DisplayName("Testing FlightNetwork")
    class FlightNetworkTest {

        private FlightNetwork flightNetwork;
        private Airport testAirport1;
        private Airport testAirport2;
        private Flight testFlight;
        private Route testRoute;
        private TestDatabaseManager testDatabaseManager;

        @BeforeEach
        void setUp() throws SQLException {
            testAirport1 = new Airport("Madrid Barajas", "MAD", "Madrid", "Spain");
            testAirport2 = new Airport("Amsterdam Schiphol", "AMS", "Amsterdam", "Netherlands");
            testFlight = new Flight("MAD", "AMS", "LAX", 2);
            testRoute = new Route("MAD", "AMS", 2);

            testDatabaseManager = new TestDatabaseManager();
            testDatabaseManager.setAirports(Arrays.asList(testAirport1, testAirport2));
            testDatabaseManager.addFlightsForAirport("MAD", Arrays.asList(testFlight));
            testDatabaseManager.addFlightsForAirport("AMS", Collections.emptyList());
            testDatabaseManager.addRoutesForAirport("MAD", Arrays.asList(testRoute));
            testDatabaseManager.addRoutesForAirport("AMS", Collections.emptyList());

            flightNetwork = new FlightNetwork() {
                {
                    this.databaseManager = testDatabaseManager;
                    loadFromDatabase();
                }
            };
        }

        @AfterEach
        void tearDown() {
            if (flightNetwork != null) {
                flightNetwork.close();
            }
        }

        @Test
        void testConstructorLoadsDataFromDatabase() throws SQLException {
            assertTrue(testDatabaseManager.wasGetAllAirportsCalled());
            assertTrue(testDatabaseManager.wasGetFlightsFromCalled());
            assertTrue(testDatabaseManager.wasGetRoutesFromCalled());
        }

        @Test
        void testConstructorThrowsSQLException() throws SQLException {
            TestDatabaseManager failingManager = new TestDatabaseManager();
            failingManager.setShouldThrowException(true);

            assertThrows(SQLException.class, () -> {
                new FlightNetwork() {
                    {
                        this.databaseManager = failingManager;
                        loadFromDatabase();
                    }
                };
            });
        }

        @Test
        void testLoadFromDatabaseSuccess() throws SQLException {
            testDatabaseManager.resetCallTracking();

            flightNetwork.loadFromDatabase();

            assertTrue(testDatabaseManager.wasGetAllAirportsCalled());
            assertTrue(testDatabaseManager.wasGetFlightsFromCalled());
            assertTrue(testDatabaseManager.wasGetRoutesFromCalled());
        }

        @Test
        void testLoadFromDatabaseThrowsException() throws SQLException {
            testDatabaseManager.setShouldThrowException(true);
            assertThrows(SQLException.class, () -> flightNetwork.loadFromDatabase());
        }

        @Test
        void testAddAirportSuccess() throws SQLException {
            Airport newAirport = new Airport("Munich", "MUC", "Munich", "Germany");

            flightNetwork.addAirport(newAirport);

            assertTrue(testDatabaseManager.wasInsertAirportCalled());
            assertEquals(newAirport, testDatabaseManager.getLastInsertedAirport());
        }

        @Test
        void testAddAirportHandlesSQLException() throws SQLException {
            Airport newAirport = new Airport("Munich", "MUC", "Munich", "Germany");
            testDatabaseManager.setShouldThrowExceptionOnInsert(true);

            assertDoesNotThrow(() -> flightNetwork.addAirport(newAirport));
            assertTrue(testDatabaseManager.wasInsertAirportCalled());
        }

        @Test
        void testAddAirportWithNull() {
            assertDoesNotThrow(() -> flightNetwork.addAirport(null));
        }

        @Test
        void testAddRouteSuccess() throws SQLException {
            Route newRoute = new Route("AMS", "VIE", 700);

            flightNetwork.addRoute(newRoute);

            assertTrue(testDatabaseManager.wasInsertRouteCalled());
            assertEquals(newRoute, testDatabaseManager.getLastInsertedRoute());
        }

        @Test
        void testAddRouteHandlesSQLException() throws SQLException {
            Route newRoute = new Route("AMS", "VIE", 700);
            testDatabaseManager.setShouldThrowExceptionOnInsert(true);

            assertDoesNotThrow(() -> flightNetwork.addRoute(newRoute));
            assertTrue(testDatabaseManager.wasInsertRouteCalled());
        }

        @Test
        void testAddRouteWithNull() {
            assertDoesNotThrow(() -> flightNetwork.addRoute(null));
        }

        @Test
        void testAddFlightSuccess() throws SQLException {
            Flight newFlight = new Flight("MAD", "AMS", "LAX", 2);

            flightNetwork.addFlight(newFlight);

            assertTrue(testDatabaseManager.wasInsertFlightCalled());
            assertEquals(newFlight, testDatabaseManager.getLastInsertedFlight());
        }

        @Test
        void testAddFlightHandlesSQLException() throws SQLException {
            Flight newFlight = new Flight("MAD", "AMS", "LAX", 2);
            testDatabaseManager.setShouldThrowExceptionOnInsert(true);

            assertDoesNotThrow(() -> flightNetwork.addFlight(newFlight));
            assertTrue(testDatabaseManager.wasInsertFlightCalled());
        }

        @Test
        void testAddFlightWithNull() {
            assertDoesNotThrow(() -> flightNetwork.addFlight(null));
        }

        @Test
        void testGetRoutesFromAirportObject() {
            List<Route> routes = flightNetwork.getRoutesFrom(testAirport1);
            assertNotNull(routes);
            assertTrue(routes.isEmpty());
        }

        @Test
        void testGetFlightsFromThrowsException() throws SQLException {
            testDatabaseManager.addExceptionForAirport("INVALID");
            assertThrows(SQLException.class, () -> flightNetwork.getFlightsFrom("INVALID"));
        }

        @Test
        void testGetFlightsFromValidAirport() throws SQLException {
            List<Flight> flights = flightNetwork.getFlightsFrom("MAD");
            assertNotNull(flights);
            assertEquals(1, flights.size());
            assertEquals(testFlight, flights.get(0));
        }

        @Test
        void testGetFlightsFromNonExistentAirport() throws SQLException {
            List<Flight> flights = flightNetwork.getFlightsFrom("NONEXISTENT");
            assertNotNull(flights);
            assertTrue(flights.isEmpty());
        }

        @Test
        void testGetAirport() throws SQLException {
            testDatabaseManager.addAirportByCode("MAD", testAirport1);

            Airport result = flightNetwork.getAirport("MAD");

            assertEquals(testAirport1, result);
            assertTrue(testDatabaseManager.wasGetAirportCalled());
        }

        @Test
        void testGetAirportThrowsException() throws SQLException {
            testDatabaseManager.addExceptionForAirport("INVALID");
            assertThrows(SQLException.class, () -> flightNetwork.getAirport("INVALID"));
        }

        @Test
        void testGetAirportNonExistent() throws SQLException {
            Airport result = flightNetwork.getAirport("NONEXISTENT");
            assertNull(result);
        }

        @Test
        void testGetRoutesFromStringThrowsRuntimeException() throws SQLException {
            testDatabaseManager.addExceptionForAirport("INVALID");
            assertThrows(RuntimeException.class, () -> flightNetwork.getRoutesFrom("INVALID"));
        }

        @Test
        void testGetRoutesFromValidString() throws SQLException {
            List<Route> routes = flightNetwork.getRoutesFrom("MAD");
            assertNotNull(routes);
            assertEquals(1, routes.size());
            assertEquals(testRoute, routes.get(0));
        }

        @Test
        void testGetRouteFound() throws SQLException {
            List<Route> routes = Arrays.asList(testRoute);
            testDatabaseManager.addRoutesForAirport("MAD", routes);

            Route result = flightNetwork.getRoute("MAD", "AMS");

            assertEquals(testRoute, result);
            assertNotNull(result);
        }

        @Test
        void testGetRouteNotFound() throws SQLException {
            List<Route> routes = Arrays.asList(testRoute);
            testDatabaseManager.addRoutesForAirport("MAD", routes);

            Route result = flightNetwork.getRoute("MAD", "MUC");

            assertNull(result);
        }

        @Test
        void testGetRouteFromNonExistentOrigin() throws SQLException {
            testDatabaseManager.addRoutesForAirport("INVALID", Collections.emptyList());

            Route result = flightNetwork.getRoute("INVALID", "MAD");

            assertNull(result);
        }

        @Test
        void testGetRouteWithNullParameters() throws SQLException {
            Route result1 = flightNetwork.getRoute(null, "AMS");
            assertNull(result1);

            Route result2 = flightNetwork.getRoute("MAD", null);
            assertNull(result2);

            Route result3 = flightNetwork.getRoute(null, null);
            assertNull(result3);
        }

        @Test
        void testGetTotalRoutes() throws SQLException {
            testDatabaseManager.setTotalRoutes(42);

            Integer result = flightNetwork.getTotalRoutes();

            assertEquals(42, result);
            assertNotEquals(0, result);
            assertNotEquals(41, result);
            assertNotEquals(43, result);
            assertTrue(testDatabaseManager.wasGetTotalRoutesCalled());
        }

        @Test
        void testGetTotalRoutesZero() throws SQLException {
            testDatabaseManager.setTotalRoutes(0);

            Integer result = flightNetwork.getTotalRoutes();

            assertEquals(0, result);
            assertNotNull(result);
        }

        @Test
        void testGetTotalAirports() throws SQLException {
            testDatabaseManager.setTotalAirports(15);

            Integer result = flightNetwork.getTotalAirports();

            assertEquals(15, result);
            assertNotEquals(0, result);
            assertNotEquals(14, result);
            assertNotEquals(16, result);
            assertTrue(testDatabaseManager.wasGetTotalAirportsCalled());
        }

        @Test
        void testGetTotalFlights() throws SQLException {
            testDatabaseManager.setTotalFlights(128);

            Integer result = flightNetwork.getTotalFlights();

            assertEquals(128, result);
            assertNotEquals(0, result);
            assertNotEquals(127, result);
            assertNotEquals(129, result);
            assertTrue(testDatabaseManager.wasGetTotalFlightsCalled());
        }

        @Test
        void testGetAllAirports() throws SQLException {
            List<Airport> airports = flightNetwork.getAllAirports();
            assertNotNull(airports);
            assertEquals(2, airports.size());
            assertTrue(airports.contains(testAirport1));
            assertTrue(airports.contains(testAirport2));
        }

        @Test
        void testClose() throws SQLException {
            flightNetwork.close();
            assertTrue(testDatabaseManager.wasCloseCalled());
        }

        @Test
        void testCloseHandlesSQLException() throws SQLException {
            testDatabaseManager.setShouldThrowExceptionOnClose(true);

            assertDoesNotThrow(() -> flightNetwork.close());
            assertTrue(testDatabaseManager.wasCloseCalled());
        }

        @Test
        void testCloseWithNullDatabaseManager() throws SQLException {
            FlightNetwork networkWithNullManager = new FlightNetwork() {
                {
                    this.databaseManager = null;
                }
            };

            assertDoesNotThrow(() -> networkWithNullManager.close());
        }

        @Test
        void testEmptyDatabase() throws SQLException {
            TestDatabaseManager emptyManager = new TestDatabaseManager();
            emptyManager.setAirports(Collections.emptyList());

            FlightNetwork emptyNetwork = new FlightNetwork() {
                {
                    this.databaseManager = emptyManager;
                    loadFromDatabase();
                }
            };

            List<Airport> airports = emptyNetwork.getAllAirports();
            assertNotNull(airports);
            assertTrue(airports.isEmpty());
            assertEquals(0, airports.size());
        }

        @Test
        void testMultipleOperations() throws SQLException {
            Airport newAirport = new Airport("London Heathrow", "LHR", "London", "United Kingdom");
            Route newRoute = new Route("LHR", "MAD", 400);
            Flight newFlight = new Flight("LHR", "MAD", "British", 2);

            flightNetwork.addAirport(newAirport);
            flightNetwork.addRoute(newRoute);
            flightNetwork.addFlight(newFlight);

            assertTrue(testDatabaseManager.wasInsertAirportCalled());
            assertTrue(testDatabaseManager.wasInsertRouteCalled());
            assertTrue(testDatabaseManager.wasInsertFlightCalled());

            // Verify the correct objects were inserted
            assertEquals(newAirport, testDatabaseManager.getLastInsertedAirport());
            assertEquals(newRoute, testDatabaseManager.getLastInsertedRoute());
            assertEquals(newFlight, testDatabaseManager.getLastInsertedFlight());
        }

        @Test
        void testSequentialOperations() throws SQLException {
            // Test that operations work correctly in sequence
            testDatabaseManager.resetCallTracking();

            Integer totalAirports = flightNetwork.getTotalAirports();
            Integer totalRoutes = flightNetwork.getTotalRoutes();
            Integer totalFlights = flightNetwork.getTotalFlights();

            assertTrue(testDatabaseManager.wasGetTotalAirportsCalled());
            assertTrue(testDatabaseManager.wasGetTotalRoutesCalled());
            assertTrue(testDatabaseManager.wasGetTotalFlightsCalled());
        }
    }

    // Test Double implementation of DatabaseManager
    private static class TestDatabaseManager extends DatabaseManager {
        private List<Airport> airports = Collections.emptyList();
        private java.util.Map<String, List<Flight>> flightsByAirport = new java.util.HashMap<>();
        private java.util.Map<String, List<Route>> routesByAirport = new java.util.HashMap<>();
        private java.util.Map<String, Airport> airportsByCode = new java.util.HashMap<>();
        private java.util.Set<String> exceptionsForAirports = new java.util.HashSet<>();

        private boolean shouldThrowException = false;
        private boolean shouldThrowExceptionOnInsert = false;
        private boolean shouldThrowExceptionOnClose = false;

        private int totalRoutes = 0;
        private int totalAirports = 0;
        private int totalFlights = 0;

        // Call tracking
        private boolean getAllAirportsCalled = false;
        private boolean getFlightsFromCalled = false;
        private boolean getRoutesFromCalled = false;
        private boolean insertAirportCalled = false;
        private boolean insertRouteCalled = false;
        private boolean insertFlightCalled = false;
        private boolean getAirportCalled = false;
        private boolean getTotalRoutesCalled = false;
        private boolean getTotalAirportsCalled = false;
        private boolean getTotalFlightsCalled = false;
        private boolean closeCalled = false;

        // Last inserted items
        private Airport lastInsertedAirport;
        private Route lastInsertedRoute;
        private Flight lastInsertedFlight;

        /**
         * Constructor to establish database connection
         *
         * @throws SQLException if database connection or table creation fails
         */
        public TestDatabaseManager() throws SQLException {
        }

        @Override
        public List<Airport> getAllAirports() throws SQLException {
            getAllAirportsCalled = true;
            if (shouldThrowException) {
                throw new SQLException("Database connection failed");
            }
            return airports;
        }

        @Override
        public List<Flight> getFlightsFrom(String airportCode) throws SQLException {
            getFlightsFromCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Airport not found");
            }
            return flightsByAirport.getOrDefault(airportCode, Collections.emptyList());
        }

        @Override
        public List<Route> getRoutesFrom(String airportCode) throws SQLException {
            getRoutesFromCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Database error");
            }
            return routesByAirport.getOrDefault(airportCode, Collections.emptyList());
        }

        @Override
        public Airport getAirport(String airportCode) throws SQLException {
            getAirportCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Airport not found");
            }
            return airportsByCode.get(airportCode);
        }

        @Override
        public void insertAirport(Airport airport) throws SQLException {
            insertAirportCalled = true;
            lastInsertedAirport = airport;
            if (shouldThrowExceptionOnInsert) {
                throw new SQLException("Insert failed");
            }
        }

        @Override
        public void insertRoute(Route route) throws SQLException {
            insertRouteCalled = true;
            lastInsertedRoute = route;
            if (shouldThrowExceptionOnInsert) {
                throw new SQLException("Insert failed");
            }
        }

        @Override
        public void insertFlight(Flight flight) throws SQLException {
            insertFlightCalled = true;
            lastInsertedFlight = flight;
            if (shouldThrowExceptionOnInsert) {
                throw new SQLException("Insert failed");
            }
        }

        @Override
        public Integer getTotalRoutes() throws SQLException {
            getTotalRoutesCalled = true;
            return totalRoutes;
        }

        @Override
        public Integer getTotalAirports() throws SQLException {
            getTotalAirportsCalled = true;
            return totalAirports;
        }

        @Override
        public Integer getTotalFlights() throws SQLException {
            getTotalFlightsCalled = true;
            return totalFlights;
        }

        @Override
        public void close() throws SQLException {
            closeCalled = true;
            if (shouldThrowExceptionOnClose) {
                throw new SQLException("Close failed");
            }
        }

        // Test setup methods
        public void setAirports(List<Airport> airports) {
            this.airports = airports;
        }

        public void addFlightsForAirport(String airportCode, List<Flight> flights) {
            flightsByAirport.put(airportCode, flights);
        }

        public void addRoutesForAirport(String airportCode, List<Route> routes) {
            routesByAirport.put(airportCode, routes);
        }

        public void addAirportByCode(String code, Airport airport) {
            airportsByCode.put(code, airport);
        }

        public void addExceptionForAirport(String airportCode) {
            exceptionsForAirports.add(airportCode);
        }

        public void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        public void setShouldThrowExceptionOnInsert(boolean shouldThrow) {
            this.shouldThrowExceptionOnInsert = shouldThrow;
        }

        public void setShouldThrowExceptionOnClose(boolean shouldThrow) {
            this.shouldThrowExceptionOnClose = shouldThrow;
        }

        public void setTotalRoutes(int total) {
            this.totalRoutes = total;
        }

        public void setTotalAirports(int total) {
            this.totalAirports = total;
        }

        public void setTotalFlights(int total) {
            this.totalFlights = total;
        }

        // Call verification methods
        public boolean wasGetAllAirportsCalled() { return getAllAirportsCalled; }
        public boolean wasGetFlightsFromCalled() { return getFlightsFromCalled; }
        public boolean wasGetRoutesFromCalled() { return getRoutesFromCalled; }
        public boolean wasInsertAirportCalled() { return insertAirportCalled; }
        public boolean wasInsertRouteCalled() { return insertRouteCalled; }
        public boolean wasInsertFlightCalled() { return insertFlightCalled; }
        public boolean wasGetAirportCalled() { return getAirportCalled; }
        public boolean wasGetTotalRoutesCalled() { return getTotalRoutesCalled; }
        public boolean wasGetTotalAirportsCalled() { return getTotalAirportsCalled; }
        public boolean wasGetTotalFlightsCalled() { return getTotalFlightsCalled; }
        public boolean wasCloseCalled() { return closeCalled; }

        public Airport getLastInsertedAirport() { return lastInsertedAirport; }
        public Route getLastInsertedRoute() { return lastInsertedRoute; }
        public Flight getLastInsertedFlight() { return lastInsertedFlight; }

        public void resetCallTracking() {
            getAllAirportsCalled = false;
            getFlightsFromCalled = false;
            getRoutesFromCalled = false;
            insertAirportCalled = false;
            insertRouteCalled = false;
            insertFlightCalled = false;
            getAirportCalled = false;
            getTotalRoutesCalled = false;
            getTotalAirportsCalled = false;
            getTotalFlightsCalled = false;
            closeCalled = false;
        }
    }
}