package org.core.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.core.models.Driver;
import org.core.models.Location;

public class CalculateService {
    private final static MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    /**
     * Finds and returns the nearest available drivers to a specific pickup
     * location.
     * The results are sorted primarily by straight-line distance in ascending
     * order.
     * If multiple drivers are at the exact same distance, they are sorted
     * secondarily
     * by their driver ID in ascending alphabetical order.
     * 
     * @param availableDrivers
     * @param pickupLocation
     * @param limit            the maximum number of drivers to return
     * @return a list of the nearest {@link Driver} objects, up to the specified
     *         limit
     */
    public static List<Driver> getNearestAvailableDrivers(List<Driver> availableDrivers, Location pickupLocation,
            int limit) {
        return availableDrivers.stream()
                .sorted(
                        // 1. Sort by distance first
                        Comparator.comparing((Driver driver) -> distance(driver.getLocation(), pickupLocation))
                                // 2. If distances are identical, sort alphabetically by id
                                .thenComparing(Driver::getId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the straight-line Euclidean distance between two locations.
     * 
     * @param loc1
     * @param loc2
     * @return the Euclidean distance as a {@link BigDecimal}
     */
    private static BigDecimal distance(Location loc1, Location loc2) {
        BigDecimal diffX = loc2.x().subtract(loc1.x());
        BigDecimal diffY = loc2.y().subtract(loc1.y());

        BigDecimal sumOfSquares = diffX.multiply(diffX).add(diffY.multiply(diffY));
        return sumOfSquares.sqrt(MATH_CONTEXT);
    }
}