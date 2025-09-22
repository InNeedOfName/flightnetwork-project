package org.pi2.service.data;

import org.pi2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;


public class CsvLoader {

    private static final Logger log = LoggerFactory.getLogger(CsvLoader.class);

    public FlightNetwork loadNetwork(String airportsCsvPath, String flightsCsvPath, String routesCsvPath) throws SQLException {
        // Get actual file paths
        String airportsPath = getFilePath(airportsCsvPath);
        String flightsPath = getFilePath(flightsCsvPath);
        String routesPath = getFilePath(routesCsvPath);

        // Create FlightNetwork with same constructor as your main code
        FlightNetwork network = new FlightNetwork();

        // Load data and persist to database if using database mode
        loadAirports(network, airportsPath);
        loadFlights(network, flightsPath);
        loadRoutes(network, routesPath);

        return network;
    }

    private String getFilePath(String csvPath) {
        try {
            // First try as resource
            var resource = getClass().getClassLoader().getResource(csvPath);
            if (resource != null) {
                return resource.getFile();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // If resource loading fails, return the path as-is (might be absolute path)
        return csvPath;
    }

    private void loadAirports(FlightNetwork network, String airportsCsvPath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(airportsCsvPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    Airport airport = new Airport(
                            values[0].trim(),
                            values[1].trim(),
                            values[2].trim(),
                            values[3].trim()
                    );
                    network.addAirport(airport);
                    count++;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());

        }
    }

    private void loadFlights(FlightNetwork network, String csvPath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    try {
                        Integer cost = Integer.parseInt(values[3].trim());
                        Flight flight = new Flight(
                                values[0].trim(),
                                values[1].trim(),
                                values[2].trim(),
                                cost
                        );
                        network.addFlight(flight);
                        count++;
                    } catch (NumberFormatException e) {
log.info(e.getMessage());                    }
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    private void loadRoutes(FlightNetwork network, String routesCsvPath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(routesCsvPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    try {
                        Route route = new Route(
                                values[0].trim(),
                                values[1].trim(),
                                Integer.parseInt(values[2].trim())
                        );
                        network.addRoute(route);
                        count++;
                    } catch (NumberFormatException e) {
                        log.info(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info(e.getMessage());
        }
    }
}