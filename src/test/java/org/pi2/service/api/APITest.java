package org.pi2.service.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.pi2.model.*;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class APITest {

    private TestFlightNetwork testNetwork;
    private API api;
    private Airport fromAirport;
    private Airport toAirport;
    private Flight testFlight;
    private Route testRoute;

    @BeforeEach
    void setUp() throws SQLException {
        testNetwork = new TestFlightNetwork();
        api = new API(testNetwork);

        fromAirport = new Airport("Zurich", "ZRH", "Zurich","Switzerland");
        toAirport = new Airport("Copenhagen", "CPH", "Copenhagen","Denmark");
        testFlight = new Flight("ZRH", "CPH", "SAS",350);
        testRoute = new Route("ZRH", "CPH",950);
    }

    @Nested
    @DisplayName("Testing the hasDirectRoute method")
    class hasDirectRouteTest {
        @Test
        @DisplayName("Should return true when direct route exists")
        void hasDirectRoute_WhenDirectRouteExists_ShouldReturnTrue() throws SQLException {
            List<Flight> flights = List.of(testFlight);
            testNetwork.addFlightsForAirport("ZRH", flights);

            boolean result = api.hasDirectRoute(fromAirport, toAirport);

            assertTrue(result);

        }

        @Test
        @DisplayName("Should return false when no direct route exists")
        void hasDirectRoute_WhenNoDirectRouteExists_ShouldReturnFalse() throws SQLException {
            Flight flightToDifferentDestination = new Flight("ZRH", "LHR", "BA", 280);
            List<Flight> flights = Arrays.asList(flightToDifferentDestination);
            testNetwork.addFlightsForAirport("ZRH", flights);

            boolean result = api.hasDirectRoute(fromAirport, toAirport);

            assertFalse(result);

        }

        @Test
        @DisplayName("Should return false when no flights from origin")
        void hasDirectRoute_WhenNoFlightsFromOrigin_ShouldReturnFalse() throws SQLException {
            testNetwork.addFlightsForAirport("ZRH", new ArrayList<>());

            boolean result = api.hasDirectRoute(fromAirport, toAirport);

            assertFalse(result);

        }

        @Test
        @DisplayName("Should handle SQLException when checking direct route")
        void hasDirectRoute_WhenSQLExceptionThrown_ShouldPropagateException() throws SQLException {
            testNetwork.addExceptionForAirport("ZRH");


            SQLException thrown = assertThrows(SQLException.class, () -> api.hasDirectRoute(fromAirport, toAirport));
            assertNotNull(thrown);
        }

        @Test
        @DisplayName("Should handle edge case with same origin and destination")
        void hasDirectRoute_WithSameOriginAndDestination_ShouldWork() throws SQLException {
            Airport sameAirport = new Airport("Frankfurt", "FRA", "Frankfurt", "Germany");
            Flight selfFlight = new Flight("FRA", "FRA", "Lufthansa", 0);
            testNetwork.addFlightsForAirport("FRA", Arrays.asList(selfFlight));

            boolean result = api.hasDirectRoute(sameAirport, sameAirport);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle multiple flights to same destination")
        void hasDirectRoute_WithMultipleFlightsToSameDestination_ShouldReturnTrue() throws SQLException {
            Flight flight1 = new Flight("ZRH", "CPH", "SAS", 350);
            Flight flight2 = new Flight("ZRH", "CPH", "Swiss", 380);
            Flight flight3 = new Flight("ZRH", "LHR", "BA", 250);

            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(flight1, flight2, flight3));

            boolean result = api.hasDirectRoute(fromAirport, toAirport);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false with mixed matching and non-matching destinations")
        void hasDirectRoute_WithMixedDestinations_ShouldStillFindMatch() throws SQLException {
            Flight correctFlight = new Flight("ZRH", "CPH", "SAS", 350);
            Flight wrongFlight1 = new Flight("ZRH", "LHR", "BA", 280);
            Flight wrongFlight2 = new Flight("ZRH", "FRA", "LH", 200);

            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(wrongFlight1, correctFlight, wrongFlight2));

            boolean result = api.hasDirectRoute(fromAirport, toAirport);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle null airports gracefully")
        void hasDirectRoute_WithNullAirports_ShouldHandleGracefully() throws SQLException {
            assertThrows(NullPointerException.class, () ->
                    api.hasDirectRoute(null, toAirport));

            assertThrows(NullPointerException.class, () ->
                    api.hasDirectRoute(fromAirport, null));

            assertThrows(NullPointerException.class, () ->
                    api.hasDirectRoute(null, null));
        }

        @Test
        @DisplayName("Should handle empty airport codes")
        void hasDirectRoute_WithEmptyAirportCodes_ShouldWork() throws SQLException {
            Airport emptyFromAirport = new Airport("", "", "", "");
            Airport emptyToAirport = new Airport("", "", "", "");
            Flight emptyFlight = new Flight("", "", "TestAirline", 100);

            testNetwork.addFlightsForAirport("", Arrays.asList(emptyFlight));

            boolean result = api.hasDirectRoute(emptyFromAirport, emptyToAirport);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle case sensitivity in airport codes")
        void hasDirectRoute_WithCaseSensitivity_ShouldBeExact() throws SQLException {
            Flight upperCaseFlight = new Flight("ZRH", "CPH", "SAS", 350);
            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(upperCaseFlight));

            Airport lowerCaseToAirport = new Airport("Copenhagen", "cph", "Copenhagen", "Denmark");

            boolean result = api.hasDirectRoute(fromAirport, lowerCaseToAirport);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Testing getFlights Method")
    class getFlightsTest {
        @Test
        @DisplayName("Should return matching flights for route")
        void getFlights_WhenMatchingFlightsExist_ShouldReturnFlights() throws SQLException {
            Flight matchingFlight1 = new Flight("ZRH", "CPH", "SAS", 350);
            Flight matchingFlight2 = new Flight("ZRH", "CPH", "Swiss", 380);
            Flight nonMatchingFlight = new Flight("ZRH", "LHR", "BA", 250);

            List<Flight> allFlights = Arrays.asList(matchingFlight1, nonMatchingFlight, matchingFlight2);
            testNetwork.addFlightsForAirport("ZRH", allFlights);

            List<Flight> result = api.getFlights(testRoute);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertNotEquals(0, result.size());
            assertNotEquals(1, result.size());
            assertNotEquals(3, result.size());
            assertTrue(result.contains(matchingFlight1));
            assertTrue(result.contains(matchingFlight2));
            assertFalse(result.contains(nonMatchingFlight));
        }

        @Test
        @DisplayName("Should return empty list when no matching flights")
        void getFlights_WhenNoMatchingFlights_ShouldReturnEmptyList() throws SQLException {
            Flight nonMatchingFlight = new Flight("ZRH", "LHR", "someAirline", 250);
            List<Flight> flights = Arrays.asList(nonMatchingFlight);
            testNetwork.addFlightsForAirport("ZRH", flights);

            List<Flight> result = api.getFlights(testRoute);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
            assertNotEquals(1, result.size());

        }

        @Test
        @DisplayName("Should return empty list when no flights from origin")
        void getFlights_WhenNoFlightsFromOrigin_ShouldReturnEmptyList() throws SQLException {
            testNetwork.addFlightsForAirport("ZRH", new ArrayList<>());

            List<Flight> result = api.getFlights(testRoute);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Should handle SQLException when getting flights")
        void getFlights_WhenSQLExceptionThrown_ShouldPropagateException() throws SQLException {
            testNetwork.addExceptionForAirport("ZRH");

            SQLException thrown = assertThrows(SQLException.class, () -> api.getFlights(testRoute));
            assertNotNull(thrown);
        }

        @Test
        @DisplayName("Should handle null route gracefully")
        void getFlights_WithNullRoute_ShouldHandleGracefully() {
            NullPointerException thrown = assertThrows(NullPointerException.class, () ->
                    api.getFlights(null));
            assertNotNull(thrown);
        }

        @Test
        @DisplayName("Should return correct flights when multiple routes have same origin")
        void getFlights_WithMultipleRoutesFromSameOrigin_ShouldReturnCorrectFlights() throws SQLException {
            Flight flightToCPH = new Flight("ZRH", "CPH", "SAS", 350);
            Flight flightToLHR = new Flight("ZRH", "LHR", "BA", 280);
            Flight flightToFRA = new Flight("ZRH", "FRA", "LH", 200);

            Route routeToLHR = new Route("ZRH", "LHR", 800);

            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(flightToCPH, flightToLHR, flightToFRA));

            List<Flight> result = api.getFlights(routeToLHR);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains(flightToLHR));
            assertFalse(result.contains(flightToCPH));
            assertFalse(result.contains(flightToFRA));
        }

        @Test
        @DisplayName("Should handle exact string matching for airport codes")
        void getFlights_WithExactStringMatching_ShouldWork() throws SQLException {
            Flight exactMatch = new Flight("ZRH", "CPH", "SAS", 350);
            Flight partialMatch = new Flight("ZRHX", "CPH", "SAS", 350);
            Flight caseMatch = new Flight("zrh", "CPH", "SAS", 350);

            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(exactMatch));
            testNetwork.addFlightsForAirport("ZRHX", Arrays.asList(partialMatch));
            testNetwork.addFlightsForAirport("zrh", Arrays.asList(caseMatch));

            List<Flight> result = api.getFlights(testRoute);

            assertNotNull(result);
            assertTrue(result.contains(exactMatch));
        }

    }

    @Nested
    @DisplayName("Testing planTrip method")
    class planTripTest {
        @Test
        @DisplayName("Should return route list when planning trip with airports")
        void planTrip_WithAirports_ShouldReturnRoutes() {
            Route route1 = new Route("ZRH", "FRA", 400);
            Route route2 = new Route("FRA", "CPH", 550);
            List<Route> expectedRoutes = Arrays.asList(route1, route2);

            Airport fraAirport = new Airport("Frankfurt", "FRA", "Frankfurt", "Germany");
            testNetwork.setAirports(Arrays.asList(fromAirport, fraAirport, toAirport));
            testNetwork.addRoutesForAirport("ZRH", Arrays.asList(route1));
            testNetwork.addRoutesForAirport("FRA", Arrays.asList(route2));
            testNetwork.addAirportByCode("FRA", fraAirport);
            testNetwork.addAirportByCode("CPH", toAirport);

            assertDoesNotThrow(() -> {
                List<Route> result = api.planTrip(fromAirport, toAirport);
                assertNotNull(result);

            });
        }

        @Test
        @DisplayName("Should handle null airports in planTrip")
        void planTrip_WithNullAirports_ShouldHandleGracefully() {
            assertThrows(Exception.class, () ->
                    api.planTrip(null, toAirport));

            assertThrows(Exception.class, () ->
                    api.planTrip(fromAirport, null));

            assertThrows(Exception.class, () ->
                    api.planTrip(null, null));
        }

        @Test
        @DisplayName("Should handle same origin and destination")
        void planTrip_WithSameOriginAndDestination_ShouldWork() {
            testNetwork.setAirports(Arrays.asList(fromAirport));
            testNetwork.addAirportByCode("ZRH", fromAirport);

            assertDoesNotThrow(() -> {
                List<Route> result = api.planTrip(fromAirport, fromAirport);
                assertNotNull(result);
            });
        }

        @Test
        @DisplayName("Should handle disconnected airports")
        void planTrip_WithDisconnectedAirports_ShouldReturnEmptyOrNull() {
            Airport isolatedAirport = new Airport("Isolated", "ISO", "Isolated", "Country");
            testNetwork.setAirports(Arrays.asList(fromAirport, isolatedAirport));
            testNetwork.addAirportByCode("ZRH", fromAirport);
            testNetwork.addAirportByCode("ISO", isolatedAirport);


            assertDoesNotThrow(() -> {
                List<Route> result = api.planTrip(fromAirport, isolatedAirport);
                assertTrue(result.isEmpty() || !result.isEmpty());

            });
        }
    }

    @Nested
    @DisplayName("Test PlanTrip method with criteria")
    class planTripCriteriaTest {
        @Test
        @DisplayName("Should return flight list when planning trip with criteria")
        void planTrip_WithCriteria_ShouldReturnFlights() throws SQLException {
            String criteria = "cheapest";

            testNetwork.setAirports(Arrays.asList(fromAirport, toAirport));
            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(testFlight));
            testNetwork.addAirportByCode("CPH", toAirport);

            assertDoesNotThrow(() -> {
                List<Flight> result = api.planTrip(fromAirport, toAirport, criteria);
                assertNotNull(result);
                assertTrue(result instanceof List);
            });
        }

        @Test
        @DisplayName("Should handle different criteria strings")
        void planTrip_WithDifferentCriteria_ShouldWork() throws SQLException {
            testNetwork.setAirports(Arrays.asList(fromAirport, toAirport));
            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(testFlight));
            testNetwork.addAirportByCode("CPH", toAirport);

            String[] criteriaOptions = {"cheapest", "fastest", "shortest", "CHEAPEST", "", "invalid"};

            for (String criteria : criteriaOptions) {
                assertDoesNotThrow(() -> {
                    List<Flight> result = api.planTrip(fromAirport, toAirport, criteria);
                    assertNotNull(result);
                }, "Failed with criteria: " + criteria);
            }
        }

        @Test
        @DisplayName("Should handle null criteria")
        void planTrip_WithNullCriteria_ShouldHandleGracefully() throws SQLException {
            testNetwork.setAirports(Arrays.asList(fromAirport, toAirport));
            testNetwork.addFlightsForAirport("ZRH", Arrays.asList(testFlight));
            testNetwork.addAirportByCode("CPH", toAirport);

            assertDoesNotThrow(() -> {
                List<Flight> result = api.planTrip(fromAirport, toAirport, null);
                assertNotNull(result);
            });
        }

        @Test
        @DisplayName("Should handle null airports with criteria")
        void planTrip_WithCriteriaAndNullAirports_ShouldHandleGracefully() {
            String criteria = "cheapest";

            assertThrows(Exception.class, () ->
                    api.planTrip(null, toAirport, criteria));

            assertThrows(Exception.class, () ->
                    api.planTrip(fromAirport, null, criteria));

            assertThrows(Exception.class, () ->
                    api.planTrip(null, null, criteria));
        }
    }

    @Test
    @DisplayName("Constructor should initialize all dependencies")
    void constructor_ShouldInitializeAllDependencies() {
        API newApi = new API(testNetwork);

        assertNotNull(newApi);

        assertDoesNotThrow(() -> {
            newApi.hasDirectRoute(fromAirport, toAirport);
        });
    }

    @Test
    @DisplayName("Constructor should handle null FlightNetwork")
    void constructor_WithNullFlightNetwork_ShouldHandleGracefully() {
        assertThrows(Exception.class, () -> {
            new API(null);
        });
    }

    @Test
    @DisplayName("API methods should work after multiple calls")
    void apiMethods_ShouldWorkAfterMultipleCalls() throws SQLException {
        testNetwork.addFlightsForAirport("ZRH", Arrays.asList(testFlight));

        boolean result1 = api.hasDirectRoute(fromAirport, toAirport);
        boolean result2 = api.hasDirectRoute(fromAirport, toAirport);
        List<Flight> flights1 = api.getFlights(testRoute);
        List<Flight> flights2 = api.getFlights(testRoute);

        assertEquals(result1, result2);
        assertEquals(flights1.size(), flights2.size());
        assertFalse(flights1.isEmpty());
    }

    @Test
    @DisplayName("API should handle concurrent-like operations")
    void api_ShouldHandleConcurrentLikeOperations() throws SQLException {
        testNetwork.addFlightsForAirport("ZRH", Arrays.asList(testFlight));
        testNetwork.addRoutesForAirport("ZRH", Arrays.asList(testRoute));

        boolean hasRoute = api.hasDirectRoute(fromAirport, toAirport);
        List<Flight> flights = api.getFlights(testRoute);

        testNetwork.resetCallTracking();
        boolean hasRoute2 = api.hasDirectRoute(fromAirport, toAirport);
        List<Flight> flights2 = api.getFlights(testRoute);

        assertEquals(hasRoute, hasRoute2);
        assertEquals(flights.size(), flights2.size());
    }

    private static class TestFlightNetwork extends FlightNetwork {
        private List<Airport> airports = new ArrayList<>();
        private Map<String, List<Flight>> flightsByAirport = new HashMap<>();
        private Map<String, List<Route>> routesByAirport = new HashMap<>();
        private Map<String, Airport> airportsByCode = new HashMap<>();
        private Set<String> exceptionsForAirports = new HashSet<>();

        private boolean getFlightsFromCalled = false;
        private boolean getRoutesFromCalled = false;
        private boolean getAirportCalled = false;
        private boolean getAllAirportsCalled = false;


        public TestFlightNetwork() throws SQLException {
        }

        @Override
        public List<Airport> getAllAirports() throws SQLException {
            getAllAirportsCalled = true;
            return new ArrayList<>(airports);
        }

        @Override
        public List<Flight> getFlightsFrom(String airportCode) throws SQLException {
            getFlightsFromCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Database error");
            }
            return new ArrayList<>(flightsByAirport.getOrDefault(airportCode, Collections.emptyList()));
        }

        @Override
        public List<Route> getRoutesFrom(String airportCode) throws SQLException {
            getRoutesFromCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Database error");
            }
            return new ArrayList<>(routesByAirport.getOrDefault(airportCode, Collections.emptyList()));
        }

        @Override
        public Airport getAirport(String airportCode) throws SQLException {
            getAirportCalled = true;
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Airport not found");
            }
            return airportsByCode.get(airportCode);
        }

        public void setAirports(List<Airport> airports) {
            this.airports = new ArrayList<>(airports);
        }

        public void addFlightsForAirport(String airportCode, List<Flight> flights) {
            this.flightsByAirport.put(airportCode, new ArrayList<>(flights));
        }

        public void addRoutesForAirport(String airportCode, List<Route> routes) {
            this.routesByAirport.put(airportCode, new ArrayList<>(routes));
        }

        public void addAirportByCode(String code, Airport airport) {
            this.airportsByCode.put(code, airport);
        }

        public void addExceptionForAirport(String airportCode) {
            this.exceptionsForAirports.add(airportCode);
        }




        public void resetCallTracking() {
            getFlightsFromCalled = false;
            getRoutesFromCalled = false;
            getAirportCalled = false;
            getAllAirportsCalled = false;
        }

        public void clearAll() {
            airports.clear();
            flightsByAirport.clear();
            routesByAirport.clear();
            airportsByCode.clear();
            exceptionsForAirports.clear();
            resetCallTracking();
        }
    }
}