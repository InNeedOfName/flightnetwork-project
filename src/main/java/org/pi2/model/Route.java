package org.pi2.model;

/**
 * Represents a flight route in the flight network
 * Can be considered a weighted Edge for the task 3
 **/
public class Route {
    // Route attributes
    private final String originCode; // IATA Code of the originating Airport of the route
    private final String destinationCode; // IATA Code of the destination Airport of the route
    private final Integer distanceInKilometer; // The distance of the route, used as an edge wieght for Task 3

    /**
     * Constructor to create a route
     * @param originCode IATA Code of the originating Airport
     * @param destinationCode IATA Code of the destination airport
     * @param distanceInKilometer Distance in kilometers, can be used as weight for task 3
     */
    public Route(String originCode, String destinationCode, Integer distanceInKilometer) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.distanceInKilometer = distanceInKilometer;
    }

    /**
     * Get IATA Code of the destination airport
     * @return IATA Code of destination Airport
     */
    public String getDestinationCode() {
        return destinationCode;
    }

    /**
     * Get IATA Code of the Originating airport
     * @return IATA Code of origin Airport
     */
    public String getOriginCode() {
        return originCode;
    }

    /**
     * Get the distance of the route in kilometers, Integer only
     * @return Distance of rout ein kilometers
     */
    public Integer getDistanceInKilometer() {
        return distanceInKilometer;
    }

    /**
     * Formatting method for representation and debugging
     * @return Route in String Format
     */
    @Override
    public String toString() {
        return String.format("%s → %s (%d km)",
                originCode, destinationCode, distanceInKilometer);
    }
}

