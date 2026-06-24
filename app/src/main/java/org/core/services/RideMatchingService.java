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

    /**
     * Requests a ride by looking for the nearest available drivers within a
     * specified limit.
     * If a driver is already. locked by another transaction, it skips to the next
     * candidate. If no drivers are successfully
     * matched, the process retries up to 3 times with a 1-second delay between
     * attempts.
     *
     * @param pickupLocation
     * @param limit          the maximum number of nearby drivers to evaluate per
     *                       attempt
     * @return a newly created {@link Ride} instance assigned to a locked driver
     * @throws IllegalStateException if no driver could be matched after all retries
     *                               are exhausted
     * @throws RuntimeException
     */
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

    /**
     * Marks an active ride as completed and restores the assigned driver's
     * availability status.
     * 
     * @param rideId the unique identifier of the ride to be completed
     * @throws IllegalArgumentException if no ride matches the provided ID
     * @throws IllegalStateException    if the specified ride has already been
     *                                  completed
     */
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