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

    private static BigDecimal distance(Location loc1, Location loc2) {
        BigDecimal diffX = loc2.x().subtract(loc1.x());
        BigDecimal diffY = loc2.y().subtract(loc1.y());

        BigDecimal sumOfSquares = diffX.multiply(diffX).add(diffY.multiply(diffY));
        return sumOfSquares.sqrt(MATH_CONTEXT);
    }
}