package ca.cmpt276.magnesium.restaurantmodel;

/**
 * SFU CMPT 276
 * Term Project - Team Magnesium
 *
 * Restaurant Model: Facility class.
 * Created by DatabaseReader's Facility factory.
 *
 * Contains data about an individual Facility.
 * Currently, the only supported facility is a Restaurant.
 *
 * @author dlutz
 *
 */

public class Facility {

    private String trackingNumber;
    private String name;
    private String address;
    private String city;
    private String dateString;
    private FacilityType facilityType;
    private double latitude;
    private double longitude;

    public Facility(String tracking, String resName, String addr,
                     String resCity, String facType, double resLatitude, double resLongitude){
        trackingNumber = tracking;
        name = resName;
        address = addr;
        city = resCity;
        latitude = resLatitude;
        longitude = resLongitude;
        if (facType.equalsIgnoreCase("restaurant")){
            facilityType = FacilityType.Restaurant;
        } else {
            /*
               Since we do not have more options right now, keep it equal to restaurants
               When we add more, we can change this
             */
            facilityType = FacilityType.Restaurant;
        }

    }
//--------------------------------------------------
    // add for testing UI
    private int iconID;

    public Facility(String trackingNumber, String name, String address, String city,
                    FacilityType facilityType, double latitude, double longitude, int iconID,
                    String dateString) {
        this.trackingNumber = trackingNumber;
        this.name = name;
        this.address = address;
        this.city = city;
        this.facilityType = facilityType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.iconID = iconID;
        this.dateString = dateString;
    }

    public int getIconID() {
        return iconID;
    }

//--------------------------------------------------------------------------------------------





    // Getters and setters provided for use by other classes.
    // Any string representation of this object should be called via this object's toString()
    // method.

    public String toString() {
        String stringRepresentation = this.name + ", "
                                    + this.address + ", "
                                    + this.city + ", "
                                    + this.trackingNumber;

        return stringRepresentation;
    }

    public String getDateString() {
        if (dateString != null) {
            return dateString;
        } else {
            return "N/A";
        }
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public FacilityType getFacilityType() {
        return facilityType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
