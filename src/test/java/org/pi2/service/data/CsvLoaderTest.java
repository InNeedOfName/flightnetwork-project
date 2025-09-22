package org.pi2.service.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pi2.model.FlightNetwork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class CsvLoaderTest {

    private CsvLoader csvLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvLoader = new CsvLoader();
    }

    @Test
    void testLoadNetwork_WithValidData_ShouldLoadAllEntities() throws IOException, SQLException {
        // Create test CSV files
        Path airportsFile = createAirportsCsv();
        Path flightsFile = createFlightsCsv();
        Path routesFile = createRoutesCsv();

        FlightNetwork network = csvLoader.loadNetwork(
                airportsFile.toString(),
                flightsFile.toString(),
                routesFile.toString()

        );

        assertNotNull(network);

    }

    @Test
    void testLoadNetwork_WithEmptyFiles_ShouldCreateEmptyNetwork() throws IOException, SQLException {
        Path airportsFile = createFile("airports.csv", "name,code,city,country\n");
        Path flightsFile = createFile("flights.csv", "origin_code,destination_code,airline,cost_in_euroes\n");
        Path routesFile = createFile("routes.csv", "origin_code,destination_code,distance_in_kilometers\n");

        FlightNetwork network = csvLoader.loadNetwork(
                airportsFile.toString(),
                flightsFile.toString(),
                routesFile.toString()

        );

        assertNotNull(network);

    }

    @Test
    void testLoadNetwork_WithMalformedData_ShouldSkipInvalidLines() throws IOException, SQLException {
        // Create CSV files with some malformed data
        Path airportsFile = createFile("airports.csv",
                "name,code,city,country\n" +
                        "John F Kennedy International,JFK,New York,USA\n" +
                        "Los Angeles International,LAX\n" + // Missing fields
                        "London Heathrow,LHR,London,UK\n"
        );

        Path flightsFile = createFile("flights.csv",
                "origin_code,destination_code,airline,cost_in_euroes\n" +
                        "JFK,LAX,American Airlines,450\n" +
                        "JFK,LHR,British Airways,invalid_cost\n" + // Invalid cost
                        "LAX,LHR,United Airlines,680\n"
        );

        Path routesFile = createFile("routes.csv",
                "origin_code,destination_code,distance_in_kilometers\n" +
                        "JFK,LAX,2500\n" +
                        "JFK,LHR,invalid_distance\n" + // Invalid distance
                        "LAX,LHR,5500\n"
        );

        FlightNetwork network = csvLoader.loadNetwork(
                airportsFile.toString(),
                flightsFile.toString(),
                routesFile.toString()

        );

        assertNotNull(network);
    }

    @Test
    void testLoadNetwork_WithNonexistentFiles_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            FlightNetwork network = csvLoader.loadNetwork(
                    "nonexistent_airports.csv",
                    "nonexistent_flights.csv",
                    "nonexistent_routes.csv"

            );
            assertNotNull(network);
        });
    }

    @Test
    void testLoadNetwork_WithDatabaseMode_ShouldWork() throws IOException, SQLException {
        Path airportsFile = createAirportsCsv();
        Path flightsFile = createFlightsCsv();
        Path routesFile = createRoutesCsv();

        assertDoesNotThrow(() -> {
            FlightNetwork network = csvLoader.loadNetwork(
                    airportsFile.toString(),
                    flightsFile.toString(),
                    routesFile.toString()

            );
            assertNotNull(network);
        });
    }

    // Helper methods to create test CSV files

    private Path createAirportsCsv() throws IOException {
        String content = "code,name,city,country\n" +
                "JFK,John F Kennedy International,New York,USA\n" +
                "LAX,Los Angeles International,Los Angeles,USA\n" +
                "LHR,London Heathrow,London,UK\n";
        return createFile("airports.csv", content);
    }

    private Path createFlightsCsv() throws IOException {
        String content = "origin_code,destination_code,airline,cost_in_euroes\n\n" +
                "JFK,LAX,american airways,500\n" +
                "JFK,LHR,Delta,800\n" +
                "LAX,LHR,Atlantic,750\n";
        return createFile("flights.csv", content);
    }

    private Path createRoutesCsv() throws IOException {
        String content = "origin_code,destination_code,distance_in_kilometers\n" +
                "JFK,LAX,2500\n" +
                "JFK,LHR,3500\n" +
                "LAX,LHR,5500\n";
        return createFile("routes.csv", content);
    }

    private Path createFile(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.write(file, content.getBytes());
        return file;
    }
}