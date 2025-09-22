package org.pi2.model;
import java.util.Objects;

/**
 * Represents the Airport in our flightnetwork system
 * THis entitiy/value object encapsulates Airport information
 */
public class Airport {
    // The Attributes of an Airport Object
    private final String name; //Full name of the Airport
    private final String code; //International Air Transport Association code of the Airport
    private final String city; // City where Airport is located
    private final String country; // Country where the Airport is located

    /**
     * Constructor creates Airport Object
     * All params are required to describe the Airport
     * @param name Full Name of the Airport i.e. "London Heathrow"
     * @param code The International Air Transport Association code of the Airport i.e. "LHR"
     * @param city Name of the City of the Airport location
     * @param country name of the Country of the Airport location, refers to the organizational structure of the Airlines. I.e. London Heathrow is in the united Kingdom and not in England
     */
    public Airport(String name, String code, String city, String country) {
        this.name = name;
        this.code = code;
        this.city = city;
        this.country = country;
    }

    /**
     * Get the Full Name of the airport
     * @return Full Name of the Airport
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Code of the Airport
     * @return Code of the Airport
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the City of the Airport
     * @return City of the Airport
     */
    public String getCity() {
        return city;
    }

    /**
     * Get the Country of the Airport
     * @return Country of the Airport
     */
    public String getCountry() {
        return country;
    }

    /**
     * Defining equality based on airport codes
     * Used for the TripPLanner
     * Airports are used as keys in hashmap collections of the routing algo. 2 Airports are euqal if their unique IATA Code is equal, regardless of other params
     * @param o   the reference object with which to compare.
     * @return True if both Obejcts have the equals iata code
     */
    @Override
    public boolean equals(Object o) {
        // check for equality
        if (this == o) return true;
        // Nullcheck and typecheck
        if (o == null || getClass() != o.getClass()) return false;
        // Comparing based on airport codes
        Airport airport = (Airport) o;
        return Objects.equals(code, airport.code);
    }

    /**
     * Generates Hashcode based on the IATA Code of the airport
     * @return Object hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}

