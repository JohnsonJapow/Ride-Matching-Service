package org.core.models;

public class Ride {
    private final String rideId;
    private final Driver driver;
    private final Location pickupLocation;
    private volatile RideStatus status;

    public enum RideStatus {
        REQUESTED, COMPLETED
    }

    public Ride(String rideId, Driver driver, Location pickupLocation) {
        this.rideId = rideId;
        this.driver = driver;
        this.pickupLocation = pickupLocation;
        this.status = RideStatus.REQUESTED;
    }

    public String getId() {
        return rideId;
    }

    public Driver getDriver() {
        return driver;
    }

    public Location getPickUpLocation() {
        return pickupLocation;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}