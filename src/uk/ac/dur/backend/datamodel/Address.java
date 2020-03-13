package uk.ac.dur.backend.datamodel;

import java.io.Serializable;

import static java.lang.Math.*;

/**
 * This is a data class which stores an immutable address
 */
public class Address implements Serializable {

    private static final long serialVersionUID = 3831785070746768044L;

    private static final double RADIUS_OF_THE_EARTH = 6_371_000;//for distance

    public final String houseNumber;
    public final String postcode;
    public final double longitude;
    public final double latitude;

    /**
     * Creates a new Address
     *
     * @param houseNumber The house number
     * @param postcode    The postcode
     * @param longitude   The longitude in degrees
     * @param latitude    The latitude in degrees
     */
    public Address(String houseNumber, String postcode, double longitude, double latitude) {
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.longitude = Math.toRadians(longitude);
        this.latitude = Math.toRadians(latitude);
    }

    /**
     * Finds the distance between two addresses
     *
     * @param other The other address
     * @return The distance in meters
     */
    public double getStraightLineDistance(Address other) {
        double deltaLat = latitude - other.latitude;
        double angle = acos(sin(latitude) * sin(other.latitude) + cos(longitude) * cos(other.longitude) * cos(deltaLat));

        if (Double.isNaN(angle)) { // precision is too low which causes this to evaluate to NaN
            // this is a good enough approximation
            return hypot(deltaLat, longitude - other.longitude) * RADIUS_OF_THE_EARTH;
        } else {
            return RADIUS_OF_THE_EARTH * angle;
        }
    }

    @Override
    public String toString() {
        return "Address{" +
                "houseNumber='" + houseNumber + '\'' +
                ", postcode='" + postcode + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
