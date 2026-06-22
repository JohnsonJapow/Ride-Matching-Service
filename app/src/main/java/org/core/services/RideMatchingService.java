package org.core.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.core.models.Driver;
import org.core.models.Location;
import org.core.models.Ride;

public class RideMatchingService {
    private final Map<String, Ride> rides = new ConcurrentHashMap<>();
    private final int REQUEST_RETRY_TIME = 3;
    public static final int DEFAULT_NUMBER_OF_DRIVERS = 10;

    public Ride requestRide(Location pickupLocation, int limit) {
        for (int attempt = 1; attempt <= REQUEST_RETRY_TIME; attempt++) {
            // find the nearest available drivers
            List<Driver> sortedDrivers = CalculateService.getNearestAvailableDrivers(
                    RegisterService.getAvailableDrivers(), pickupLocation,
                    limit);
            if (sortedDrivers != null && !sortedDrivers.isEmpty()) {
                for (Driver driver : sortedDrivers) {
                    if (driver.getLock().tryLock()) {
                        try {
                            if (!driver.isAvailable())
                                continue;
                            RegisterService.setDriverAvailability(driver.getId(), false);
                            String rideId = UUID.randomUUID().toString();
                            Ride ride = new Ride(rideId, driver, pickupLocation);
                            rides.put(rideId, ride);
                            return ride;
                        } finally {
                            driver.getLock().unlock();
                        }
                    }
                }
            }
            System.out.println(
                    "No available drivers nearby or match failed. Retry " + attempt + "/" + REQUEST_RETRY_TIME);
            if (attempt < REQUEST_RETRY_TIME) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Ride matching service was interrupted during retry delay", e);
                }
            }
        }
        throw new IllegalStateException("No available drivers nearby after " + REQUEST_RETRY_TIME + " attempts.");
    }

    public void completeRide(String rideId) {
        Ride ride = rides.get(rideId);
        if (ride == null)
            throw new IllegalArgumentException("Ride not found");
        Driver driver = ride.getDriver();
        driver.getLock().lock();
        try {
            if (ride.getStatus().equals(Ride.RideStatus.COMPLETED)) {
                throw new IllegalStateException("Ride is already completed");
            }

            ride.setStatus(Ride.RideStatus.COMPLETED);
            driver.setAvailable(true);
        } finally {
            driver.getLock().unlock();
        }
    }
}