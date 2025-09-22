package org.pi2.service.planning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pi2.model.*;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TripPlannerTest {
    private TestFlightNetwork testNetwork;

    @Nested
    @DisplayName("Testing TripPlannerRoutes Method")
    class TripPlannerRouteTest {

        private TripPlannerRoute tripPlannerRoute;
        private Airport lhr, muc, bgy, cdg;
        private Route routeLhrMuc, routeMucBgy, routeLhrCdg, routeCdgBgy, routeMucCdg;

        @BeforeEach
        void setUp() throws SQLException {
            testNetwork = new TestFlightNetwork();
            tripPlannerRoute = new TripPlannerRoute(testNetwork);

            // Create test airports using the provided data
            lhr = new Airport("London Heathrow","LHR", "London", "United Kingdom");
            muc = new Airport("Munich","MUC",  "Munich", "Germany");
            bgy = new Airport("Bergamo","BGY",  "Bergamo", "Italy");
            cdg = new Airport("Paris Charles de Gaulle","CDG",  "Paris", "France");

            // Create routes
            routeLhrMuc = new Route("LHR", "MUC", 950);  // London to Munich ~950km
            routeMucBgy = new Route("MUC", "BGY", 380);  // Munich to Bergamo ~380km
            routeLhrCdg = new Route("LHR", "CDG", 460);  // London to Paris ~460km
            routeCdgBgy = new Route("CDG", "BGY", 650);  // Paris to Bergamo ~650km
            routeMucCdg = new Route("MUC", "CDG", 830);  // Munich to Paris ~830km
        }

        @Test
        void testFindShortestPath_NullParameters_ReturnsEmptyList() throws SQLException {
            // Test null from airport
            List<Route> result1 = tripPlannerRoute.findShortestPath(null, muc);
            assertTrue(result1.isEmpty());

            // Test null to airport
            List<Route> result2 = tripPlannerRoute.findShortestPath(lhr, null);
            assertTrue(result2.isEmpty());

            // Test both null
            List<Route> result3 = tripPlannerRoute.findShortestPath(null, null);
            assertTrue(result3.isEmpty());
        }

        @Test
        void testFindShortestPath_DirectRoute() throws SQLException {
            // Setup: Direct route from LHR to CDG
            testNetwork.setAirports(Arrays.asList(lhr, cdg));
            testNetwork.addRoutesForAirport("LHR", Arrays.asList(routeLhrCdg));
            testNetwork.addAirportByCode("CDG", cdg);

            List<Route> result = tripPlannerRoute.findShortestPath(lhr, cdg);

            assertEquals(1, result.size());
            assertEquals("LHR", result.get(0).getOriginCode());
            assertEquals("CDG", result.get(0).getDestinationCode());
            assertEquals(460, result.get(0).getDistanceInKilometer());
        }

        @Test
        void testFindShortestPath_MultipleHops() throws SQLException {
            // Setup: LHR->CDG (460km) + CDG->BGY (650km) = 1110km vs LHR->MUC (950km) + MUC->BGY (380km) = 1330km
            testNetwork.setAirports(Arrays.asList(lhr, muc, cdg, bgy));
            testNetwork.addRoutesForAirport("LHR", Arrays.asList(routeLhrMuc, routeLhrCdg));
            testNetwork.addRoutesForAirport("MUC", Arrays.asList(routeMucBgy, routeMucCdg));
            testNetwork.addRoutesForAirport("CDG", Arrays.asList(routeCdgBgy));
            testNetwork.addRoutesForAirport("BGY", Collections.emptyList());
            testNetwork.addAirportByCode("MUC", muc);
            testNetwork.addAirportByCode("CDG", cdg);
            testNetwork.addAirportByCode("BGY", bgy);
            testNetwork.addAirportByCode("LHR", lhr);

            List<Route> result = tripPlannerRoute.findShortestPath(lhr, bgy);

            // Should choose LHR->CDG->BGY (1110km) over LHR->MUC->BGY (1330km)
            assertEquals(2, result.size());
            assertEquals("LHR", result.get(0).getOriginCode()); // LHR->CDG
            assertEquals("CDG", result.get(0).getDestinationCode());
            assertEquals("CDG", result.get(1).getOriginCode()); // CDG->BGY
            assertEquals("BGY", result.get(1).getDestinationCode());
        }

        @Test
        void testFindShortestPath_NoRoute() throws SQLException {
            // Test with no routes from LHR to a disconnected airport
            Airport isolatedAirport = new Airport("JFK", "John F Kennedy", "New York", "USA");

            testNetwork.setAirports(Arrays.asList(lhr, isolatedAirport));
            testNetwork.addRoutesForAirport("LHR", Collections.emptyList());

            List<Route> result = tripPlannerRoute.findShortestPath(lhr, isolatedAirport);

            assertTrue(result.isEmpty());
        }

        @Test
        void testFindShortestPath_SameOriginAndDestination() throws SQLException {
            testNetwork.setAirports(Arrays.asList(lhr));

            List<Route> result = tripPlannerRoute.findShortestPath(lhr, lhr);

            assertTrue(result.isEmpty());
        }

        @Test
        void testFindShortestPath_RouteToNullAirport() throws SQLException {
            // Test when network.getAirport returns null
            testNetwork.setAirports(Arrays.asList(lhr, muc));
            testNetwork.addRoutesForAirport("LHR", Arrays.asList(routeLhrMuc));
            testNetwork.addAirportByCode("MUC", null); // Simulate null airport

            List<Route> result = tripPlannerRoute.findShortestPath(lhr, muc);

            assertTrue(result.isEmpty());
        }

        @Test
        void testFormatRoute_EmptyList() {
            String result = tripPlannerRoute.formatRoute(Collections.emptyList());
            assertEquals("No route found", result);
        }

        @Test
        void testFormatRoute_SingleRoute() {
            List<Route> routes = Arrays.asList(routeLhrCdg);
            String result = tripPlannerRoute.formatRoute(routes);
            assertEquals("LHR to CDG (Total: 460 km)", result);
        }

        @Test
        void testFormatRoute_MultipleRoutes() {
            List<Route> routes = Arrays.asList(routeLhrCdg, routeCdgBgy);
            String result = tripPlannerRoute.formatRoute(routes);
            assertEquals("LHR → CDG to BGY (Total: 1110 km)", result);
        }

        @Test
        void testPlanAndFormatRoute_Integration() throws SQLException {
            testNetwork.setAirports(Arrays.asList(lhr, cdg));
            testNetwork.addRoutesForAirport("LHR", Arrays.asList(routeLhrCdg));
            testNetwork.addAirportByCode("CDG", cdg);

            String result = tripPlannerRoute.planAndFormatRoute(lhr, cdg);
            assertEquals("LHR to CDG (Total: 460 km)", result);
        }

        @Test
        void testComplexEuropeanRouteOptimization() throws SQLException {
            // Test a complex European route scenario with multiple possible paths
            testNetwork.setAirports(Arrays.asList(lhr, muc, bgy, cdg));
            testNetwork.addRoutesForAirport("LHR", Arrays.asList(routeLhrMuc, routeLhrCdg));
            testNetwork.addRoutesForAirport("MUC", Arrays.asList(routeMucBgy, routeMucCdg));
            testNetwork.addRoutesForAirport("CDG", Arrays.asList(routeCdgBgy));
            testNetwork.addRoutesForAirport("BGY", Collections.emptyList());
            testNetwork.addAirportByCode("MUC", muc);
            testNetwork.addAirportByCode("CDG", cdg);
            testNetwork.addAirportByCode("BGY", bgy);
            testNetwork.addAirportByCode("LHR", lhr);

            // Test London to Bergamo - should find shortest distance path
            List<Route> result = tripPlannerRoute.findShortestPath(lhr, bgy);

            // Should choose LHR->CDG->BGY (460+650=1110km) over LHR->MUC->BGY (950+380=1330km)
            assertEquals(2, result.size());
            assertEquals("LHR", result.get(0).getOriginCode()); // LHR->CDG
            assertEquals("CDG", result.get(0).getDestinationCode());
            assertEquals("CDG", result.get(1).getOriginCode()); // CDG->BGY
            assertEquals("BGY", result.get(1).getDestinationCode());

            String formatted = tripPlannerRoute.formatRoute(result);
            assertEquals("LHR → CDG to BGY (Total: 1110 km)", formatted);
        }
    }

    @Nested
    @DisplayName("Testing TripPlannerFlights Method")
    class TripPlannerFlightTest {

        private TripPlannerFlight tripPlanner;
        private Airport lhr, muc, bgy, cdg;
        private Flight flightLhrMuc, flightMucBgy, flightLhrCdg, flightCdgBgy, flightMucCdg;

        @BeforeEach
        void setUp() throws SQLException {
            testNetwork = new TestFlightNetwork();
            tripPlanner = new TripPlannerFlight(testNetwork);

            // Create test airports using the provided data
            lhr = new Airport("London Heathrow","LHR", "London", "United Kingdom");
            muc = new Airport("Munich","MUC",  "Munich", "Germany");
            bgy = new Airport("Bergamo","BGY",  "Bergamo", "Italy");
            cdg = new Airport("Paris Charles de Gaulle","CDG",  "Paris", "France");

            // Create test flights between these airports (no flight numbers in schema)
            flightLhrMuc = new Flight("LHR", "MUC", "British Airways", 120);
            flightMucBgy = new Flight("MUC", "BGY", "Lufthansa", 90);
            flightLhrCdg = new Flight("LHR", "CDG", "Air France", 80);
            flightCdgBgy = new Flight("CDG", "BGY", "Air France", 110);
            flightMucCdg = new Flight("MUC", "CDG", "Lufthansa", 95);
        }

        @Test
        void testPlanTrip_NullParameters_ReturnsEmptyList() throws SQLException {
            // Test null from airport
            List<Flight> result1 = tripPlanner.planTrip(null, muc, "shortest");
            assertTrue(result1.isEmpty());

            // Test null to airport
            List<Flight> result2 = tripPlanner.planTrip(lhr, null, "shortest");
            assertTrue(result2.isEmpty());

            // Test null criteria
            List<Flight> result3 = tripPlanner.planTrip(lhr, muc, null);
            assertTrue(result3.isEmpty());
        }

        @Test
        void testPlanTrip_InvalidCriteria_ReturnsEmptyList() throws SQLException {
            List<Flight> result = tripPlanner.planTrip(lhr, muc, "invalid");
            assertTrue(result.isEmpty());
        }

        @Test
        void testFindCheapestFlights_DirectFlight() throws SQLException {
            // Setup: Direct flight from LHR to CDG
            testNetwork.setAirports(Arrays.asList(lhr, cdg));
            testNetwork.addFlightsForAirport("LHR", Arrays.asList(flightLhrCdg));
            testNetwork.addAirportByCode("CDG", cdg);

            List<Flight> result = tripPlanner.planTrip(lhr, cdg, "cheapest");

            assertEquals(1, result.size());
            assertEquals(flightLhrCdg, result.get(0));
        }

        @Test
        void testFindCheapestFlights_MultipleHops() throws SQLException {
            // Setup: LHR->CDG (cost 80) + CDG->BGY (cost 110) = 190 vs LHR->MUC (cost 120) + MUC->BGY (cost 90) = 210
            testNetwork.setAirports(Arrays.asList(lhr, muc, cdg, bgy));
            testNetwork.addFlightsForAirport("LHR", Arrays.asList(flightLhrMuc, flightLhrCdg));
            testNetwork.addFlightsForAirport("MUC", Arrays.asList(flightMucBgy, flightMucCdg));
            testNetwork.addFlightsForAirport("CDG", Arrays.asList(flightCdgBgy));
            testNetwork.addFlightsForAirport("BGY", Collections.emptyList());
            testNetwork.addAirportByCode("MUC", muc);
            testNetwork.addAirportByCode("CDG", cdg);
            testNetwork.addAirportByCode("BGY", bgy);

            List<Flight> result = tripPlanner.planTrip(lhr, bgy, "cheapest");

            // Should choose LHR->CDG->BGY (total cost 190) over LHR->MUC->BGY (cost 210)
            assertEquals(2, result.size());
            assertEquals("LHR", result.get(0).getOriginCode()); // LHR->CDG
            assertEquals("CDG", result.get(0).getDestinationCode());
            assertEquals("CDG", result.get(1).getOriginCode()); // CDG->BGY
            assertEquals("BGY", result.get(1).getDestinationCode());
        }

        @Test
        void testFindCheapestFlights_NoRoute() throws SQLException {
            // Test with no flights from LHR to a disconnected airport
            Airport isolatedAirport = new Airport("JFK", "John F Kennedy", "New York", "USA");

            testNetwork.setAirports(Arrays.asList(lhr, isolatedAirport));
            testNetwork.addFlightsForAirport("LHR", Collections.emptyList());

            List<Flight> result = tripPlanner.planTrip(lhr, isolatedAirport, "cheapest");

            assertTrue(result.isEmpty());
        }

        @Test
        void testFormatFlightTrip_EmptyList() {
            String result = tripPlanner.formatFlightTrip(Collections.emptyList());
            assertEquals("No flights found", result);
        }

        @Test
        void testFormatFlightTrip_SingleFlight() {
            List<Flight> flights = Arrays.asList(flightLhrCdg);
            String result = tripPlanner.formatFlightTrip(flights);
            assertEquals("LHR (Air France) → CDG (Total Cost: 80 €)", result);
        }

        @Test
        void testFormatFlightTrip_MultipleFlight() {
            List<Flight> flights = Arrays.asList(flightLhrMuc, flightMucBgy);
            String result = tripPlanner.formatFlightTrip(flights);
            assertEquals("LHR (British Airways) → MUC (Lufthansa) → BGY (Total Cost: 210 €)", result);
        }

        @Test
        void testPlanAndFormatTrip_Integration() throws SQLException {
            testNetwork.setAirports(Arrays.asList(lhr, cdg));
            testNetwork.addFlightsForAirport("LHR", Arrays.asList(flightLhrCdg));
            testNetwork.addAirportByCode("CDG", cdg);

            String result = tripPlanner.planAndFormatTrip(lhr, cdg, "cheapest");
            assertEquals("LHR (Air France) → CDG (Total Cost: 80 €)", result);
        }

        @Test
        void testComplexEuropeanRoute() throws SQLException {
            // Test a realistic European route scenario
            testNetwork.setAirports(Arrays.asList(lhr, muc, bgy, cdg));
            testNetwork.addFlightsForAirport("LHR", Arrays.asList(flightLhrMuc, flightLhrCdg));
            testNetwork.addFlightsForAirport("MUC", Arrays.asList(flightMucBgy, flightMucCdg));
            testNetwork.addFlightsForAirport("CDG", Arrays.asList(flightCdgBgy));
            testNetwork.addFlightsForAirport("BGY", Collections.emptyList());
            testNetwork.addAirportByCode("MUC", muc);
            testNetwork.addAirportByCode("CDG", cdg);
            testNetwork.addAirportByCode("BGY", bgy);

            // Test London to Bergamo - should find cheapest route
            List<Flight> result = tripPlanner.planTrip(lhr, bgy, "cheapest");

            // Should choose LHR->CDG->BGY (80+110=190) over LHR->MUC->BGY (120+90=210)
            assertEquals(2, result.size());
            assertEquals("LHR", result.get(0).getOriginCode()); // LHR->CDG
            assertEquals("CDG", result.get(0).getDestinationCode());
            assertEquals("CDG", result.get(1).getOriginCode()); // CDG->BGY
            assertEquals("BGY", result.get(1).getDestinationCode());

            String formatted = tripPlanner.formatFlightTrip(result);
            assertEquals("LHR (Air France) → CDG (Air France) → BGY (Total Cost: 190 €)", formatted);
        }
    }

    // Test Double implementation of FlightNetwork
    private static class TestFlightNetwork extends FlightNetwork {
        private List<Airport> airports = new ArrayList<>();
        private Map<String, List<Flight>> flightsByAirport = new HashMap<>();
        private Map<String, List<Route>> routesByAirport = new HashMap<>();
        private Map<String, Airport> airportsByCode = new HashMap<>();
        private Set<String> exceptionsForAirports = new HashSet<>();

        /**
         * Constructor initalizes the flightnetwork
         * loads data form the SQLite database
         *
         * @throws SQLException
         */
        public TestFlightNetwork() throws SQLException {
        }

        // Override methods to return test data instead of calling database
        @Override
        public List<Airport> getAllAirports() throws SQLException {
            return new ArrayList<>(airports);
        }

        @Override
        public List<Flight> getFlightsFrom(String airportCode) throws SQLException {
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Airport not found");
            }
            return flightsByAirport.getOrDefault(airportCode, Collections.emptyList());
        }

        @Override
        public List<Route> getRoutesFrom(String airportCode) throws SQLException {
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Database error");
            }
            return routesByAirport.getOrDefault(airportCode, Collections.emptyList());
        }

        @Override
        public Airport getAirport(String airportCode) throws SQLException {
            if (exceptionsForAirports.contains(airportCode)) {
                throw new SQLException("Airport not found");
            }
            return airportsByCode.get(airportCode);
        }

        // Test setup methods
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

        // Clear all test data
        public void clearAll() {
            airports.clear();
            flightsByAirport.clear();
            routesByAirport.clear();
            airportsByCode.clear();
            exceptionsForAirports.clear();
        }
    }
}