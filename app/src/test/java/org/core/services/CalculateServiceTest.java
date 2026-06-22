package org.core.services;

import org.core.models.Driver;
import org.core.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateServiceTest {
    int limit = 10;
    private RideMatchingService rideMatchingService;

    @BeforeEach
    void setUp() {
        rideMatchingService = new RideMatchingService();
        RegisterService.clearAll();
    }

    @Test
    void testGetNearestAvailableDrivers_ReturnsClosestDriversInOrder() {

        Location pickup = new Location("0", "0");

        Driver d1 = new Driver("D1", "Alice", new Location("1", "1"));
        Driver d2 = new Driver("D2", "Bob", new Location("5", "5"));
        Driver d3 = new Driver("D3", "Carol", new Location("2", "2"));

        RegisterService.registerDriver(d1.getId(), d1);
        RegisterService.registerDriver(d2.getId(), d2);
        RegisterService.registerDriver(d3.getId(), d3);

        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(), pickup,
                limit);

        assertEquals(3, result.size());
        assertEquals(d1.getId(), result.get(0).getId());
        assertEquals(d3.getId(), result.get(1).getId());
        assertEquals(d2.getId(), result.get(2).getId());
    }

    @Test
    void testGetNearestAvailableDrivers_FiltersUnavailableDrivers() {
        Location pickup = new Location("0", "0");
        Driver availableDriver = new Driver("D1", "Alice", new Location("1", "1"));

        Driver unavailableDriver = new Driver("D2", "Bob", new Location("0.1", "0.1"));

        RegisterService.registerDriver(availableDriver.getId(), availableDriver);

        RegisterService.registerDriver(unavailableDriver.getId(), unavailableDriver);

        rideMatchingService.requestRide(unavailableDriver.getLocation(), limit);
        
        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(), pickup,
                limit);

        assertEquals(1, result.size());
        assertEquals(availableDriver.getId(), result.get(0).getId());
    }

    @Test
    void testGetNearestAvailableDrivers_RespectsLimit() {
        Location pickup = new Location("0", "0");

        Driver d1 = new Driver("D1", "A", new Location("1", "0"));
        Driver d2 = new Driver("D2", "B", new Location("2", "0"));
        Driver d3 = new Driver("D3", "C", new Location("3", "0"));

        RegisterService.registerDriver(d1.getId(), d1);
        RegisterService.registerDriver(d2.getId(), d2);
        RegisterService.registerDriver(d3.getId(), d3);

        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(), pickup,
                2);

        assertEquals(2, result.size());
        assertEquals(d1.getId(), result.get(0).getId());
        assertEquals(d2.getId(), result.get(1).getId());
    }

    @Test
    void testGetNearestAvailableDrivers_NoAvailableDrivers() {
        Driver d1 = new Driver("D1", "A", new Location("1", "0"));
        Driver d2 = new Driver("D2", "B", new Location("2", "0"));

        RegisterService.registerDriver(d1.getId(), d1);
        RegisterService.registerDriver(d2.getId(), d2);

        rideMatchingService.requestRide(new Location("0", "0"), limit);
        rideMatchingService.requestRide(new Location("0", "0"), limit);

        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(),
                new Location("0", "0"),
                limit);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetNearestAvailableDrivers_EmptyRegistry() {
        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(),
                new Location("0", "0"),
                limit);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetNearestAvailableDrivers_SameDistanceDrivers() {
        Location pickup = new Location("0", "0");

        Driver d1 = new Driver("D1", "A", new Location("1", "0"));
        Driver d2 = new Driver("D2", "B", new Location("-1", "0"));

        RegisterService.registerDriver(d1.getId(), d1);
        RegisterService.registerDriver(d2.getId(), d2);

        List<Driver> result = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(), pickup,
                limit);

        assertEquals(2, result.size());
        assertEquals("D1", result.get(0).getId());
        assertEquals("D2", result.get(1).getId());
    }
}