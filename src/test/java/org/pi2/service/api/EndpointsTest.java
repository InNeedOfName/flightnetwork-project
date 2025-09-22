package org.pi2.service.api;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.pi2.model.*;
import org.pi2.service.data.DatabaseManager;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class EndpointsTest {

    private DatabaseManager databaseManager;
    private Javalin app;

    @BeforeEach
    void setUp() throws SQLException {
        databaseManager = new DatabaseManager();
        FlightNetwork flightNetwork = new FlightNetwork();
        API flightAPI = new API(flightNetwork);
        Endpoints endpoints = new Endpoints(flightNetwork, flightAPI);

        app = Javalin.create();
        endpoints.setupRoutes(app);



    }

    @AfterEach
    void tearDown() throws SQLException {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (app != null) {
            app.stop();
        }
    }


    @Test
    void testWelcomeEndpoint() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            assertTrue(response.body().string().contains("Welcome to the Flight API"));
        });
    }

    @Test
    void testHasDirectRoute_ExistingRoute() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/hasDirectRoute/ZRH/CPH");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("\"hasDirectRoute\""));
        });
    }

    @Test
    void testHasDirectRoute_NonExistingRoute() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/hasDirectRoute/CPH/ZRH");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("\"hasDirectRoute\":false"));
        });
    }

    @Test
    void testHasDirectRoute_InvalidOrigin() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/hasDirectRoute/INVALID/CPH");
            assertEquals(404, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("error"));
        });
    }

    @Test
    void testHasDirectRoute_InvalidDestination() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/hasDirectRoute/ZRH/INVALID");
            assertEquals(404, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("error"));
        });
    }

    @Test
    void testGetFlight_ExistingRoute() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/getFlight/ZRH/CPH");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("SAS"));
            assertTrue(responseBody.contains("350"));
        });
    }

    @Test
    void testGetFlight_NonExistingRoute() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/getFlight/CPH/ZRH");
            assertEquals(404, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("Route not found"));
        });
    }

    @Test
    void testPlanTripRoute_ValidAirports() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/planTripRoute/ZRH/CPH");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("Routes"));
        });
    }

    @Test
    void testPlanTripRoute_InvalidOrigin() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/planTripRoute/INVALID/CPH");
            assertEquals(404, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("Route Origin not found"));
        });
    }

    @Test
    void testPlanTripFlight_ValidRequest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/planTripFlight/ZRH/CPH/cheapest");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("Routes"));
            assertTrue(responseBody.contains("cheapest"));
        });
    }

    @Test
    void testPlanTripFlight_EmptyCriteria() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/task/planTripFlight/ZRH/CPH/");
            assertEquals(404, response.code());
        });
    }

    @Test
    void testStats() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/stats");
            assertEquals(200, response.code());
            assertNotNull(response.body());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("amount of Airports in Db"));
            assertTrue(responseBody.contains("Amount of Flights in Database"));
            assertTrue(responseBody.contains("Amount of Routes in Database"));
        });
    }
}