package org.pi2.model;

/*
 * Represents the FLight Object in the flightnetwork
 * */

public class Flight {
    // Attributes of a flight
    private final String originCode; // IATA Code of the Originating Airport
    private final String destinationCode; // IATA Code of the Destination Airport
    private final String airline; // Name of the Airline which perfoms the flight
    private final Integer CostInEuros; // The cost of the flight in Euros

    /*
    * constructor creates a flight object
    * @param originCode IATA Code of the originating Airport
    * @param destinationCode IATA Code of the destination Airport
    * @param airline Name of the Airline
    * @param CostInEuros Cost in Euros of the flight, no decimals implemented because flights are expensive
     */
    public Flight(String originCode, String destinationCode, String airline, Integer CostInEuros) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.airline = airline;
        this.CostInEuros = CostInEuros;
    }

    /**
     * Get IATA code of the originating airport
     * @return IATA code of the Originating Airport
     */
    public String getOriginCode() {
        return originCode;
    }

    /**
     * Get IATA code of the destination Airport
     * @return IATA code of the destination Airport
     */
    public String getDestinationCode() {
        return destinationCode;
    }

    /**
     * get the name of the airline used for the flight
     * @return name of the airline
     */
    public String getAirline() {
        return airline;
    }

    /**
     * Get the cost of the flight
     * Can be used as weight for the graph in task 3
     * @return Cost in Euros
     */
    public Integer getCostInEuros() {
        return CostInEuros;
    }

    /**
     * Formatting Flight as String for Debugging and representation
     * @return Flight as String
     */
    @Override
    public String toString() {
        return String.format("(%s, %s, %s, %s)",
                originCode, destinationCode, airline, CostInEuros);
    }
}


