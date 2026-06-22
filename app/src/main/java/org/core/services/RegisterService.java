package org.core.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.core.models.Driver;
import org.core.models.Location;

public class RegisterService {
    private RegisterService() {};
    private static final Map<String, Driver> drivers = new ConcurrentHashMap<>();

    public static List<Driver> getAvailableDrivers() {
        return drivers.values().stream()
                .filter(Driver::isAvailable)
                .toList();
    }

    static Optional<Driver> getDriver(String id) {
        return Optional.ofNullable(drivers.get(id));
    }

    static void updateDriverLocation(String id, Location newLocation) {
        Driver driver = drivers.get(id);
        if (driver == null) throw new IllegalArgumentException("Driver not found: " + id);
        driver.getLock().lock();
        try {
            driver.setLocation(newLocation);
        } finally {
            driver.getLock().unlock();
        }
    }

    static void setDriverAvailability(String id, boolean available) {
        Driver driver = drivers.get(id);
        if (driver == null) throw new IllegalArgumentException("Driver not found: " + id);
        driver.getLock().lock();
        try {
            driver.setAvailable(available);
        } finally {
            driver.getLock().unlock();
        }
    }
    public static void registerDriver(String id, Driver driver) {
        drivers.compute(id,
                (k, existingDriver) -> {
                    if (existingDriver != null) {
                        existingDriver.getLock().lock();
                        try {
                            existingDriver.setName(driver.getName());
                            existingDriver.setAvailable(true);
                            existingDriver.setLocation(driver.getLocation());
                            return existingDriver;
                        } finally {
                            existingDriver.getLock().unlock();
                        }
                    } else {
                        return new Driver(id, driver.getName(), driver.getLocation());
                    }
                });
    }

    // It is only used for test purpose to easily clear the drivers map for next unit test.
    static void clearAll() {
        drivers.clear();
    }
}
