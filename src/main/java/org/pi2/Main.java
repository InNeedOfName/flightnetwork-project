package org.pi2;

import io.javalin.Javalin;
import org.pi2.model.*;
import org.pi2.service.api.API;
import org.pi2.service.api.Endpoints;
import org.pi2.service.data.CsvLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;


/**
 * Main application
 * This class straps the routing system with the endpoints as well as the other components of the API
 *
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static FlightNetwork flightNetwork; // Data structure
    private static API flightAPI; // Service layer and handling of the tasks

    /**
     * Entry point for the application
     * @param args
     */
    public static void main(String[] args) {
        // Loading the flight Data from the csv into the datatbase
        initializeData();
        log.info("Database initialized");


        // Starting javalin
        Javalin app = Javalin.create().start(8080);
        log.info("API initialized");

        // passing javalin to the routes, so they can be initialized
        setupRoutes(app);
        log.info("Routes initialized");

        // Shutdown handling
        setupShutdown();
        log.info("Flightnetwork API is running on Port 8080");

    }

    /**
     * Initializes the flightnetwork
     */
    private static void initializeData() {
        try {
            log.info("Initializing database....");

            // Constructing the network
            flightNetwork = new FlightNetwork();
            loadFromCSV();
            log.info("Database initialization completed successfully");
            //initialize the service layer on the network
            flightAPI = new API(flightNetwork);

        }

        /// Error handling in case of unforeseen issues with the database

        catch (SQLException e) {
            log.error("Database init failed: {}", e.getMessage());
            flightAPI = new API(flightNetwork);
        }
    }

    /**
     * Loads flightnetwork data from csv files as fallback
     * @throws SQLException in case of db operations error
     */
    private static void loadFromCSV() throws SQLException {
        CsvLoader loader = new CsvLoader();
        String airportsFile = "airports.csv";
        String flightsFile = "flights.csv";
        String routesFile = "routes.csv";

        try {
            flightNetwork = loader.loadNetwork(airportsFile, flightsFile, routesFile);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Setting up the endpoint routing for the javalin application
     * @param app javalin instance
     */
    private static void setupRoutes(Javalin app) {
        // create endpoint controller with the network and API basis
        Endpoints endpoints = new Endpoints(flightNetwork, flightAPI);
        // configure the routes for access
        endpoints.setupRoutes(app);
        log.info("Routes setup completed successfully");
    }

    /**
     * Configures graceful shutdown
     */
    private static void setupShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down");
            // Clean up datastruct and database connections
            if (flightNetwork != null) {
                flightNetwork.close();
            }
            log.info("Database connection closed");
        }));
    }
}