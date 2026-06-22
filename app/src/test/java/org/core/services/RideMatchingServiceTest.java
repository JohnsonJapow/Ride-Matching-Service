package org.core.services;

import org.core.models.Driver;
import org.core.models.Location;
import org.core.models.Ride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RideMatchingServiceTest {

    private RideMatchingService rideMatchingService;
    private Location dummyLocation;

    @BeforeEach
    void setUp() {

        rideMatchingService = new RideMatchingService();
        RegisterService.clearAll();
        dummyLocation = new Location("0", "0");
    }

    @Test
    void testRequestRide_Success() {
        Driver driver = new Driver("D1", "Alice", dummyLocation);
        int limit = 10;
        RegisterService.registerDriver(driver.getId(), driver);

        Ride ride = rideMatchingService.requestRide(dummyLocation, limit);

        assertNotNull(ride);
        assertEquals(driver.getId(), ride.getDriver().getId());
        assertFalse(RegisterService.getDriver(driver.getId()).get().isAvailable());
        assertEquals(Ride.RideStatus.REQUESTED, ride.getStatus()); // Assumes starting status is REQUESTED
    }

    @Test
    void testRequestRide_NoDriversAvailable_ThrowsException() {
        int limit = 10;
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rideMatchingService.requestRide(dummyLocation, limit);
        });
        assertTrue(exception.getMessage().contains("No available drivers nearby"));
    }

    @Test
    void testCompleteRide_Success() {
        Driver driver = new Driver("D1", "Alice", dummyLocation);
        int limit = 10;
        RegisterService.registerDriver(driver.getId(), driver);
        Ride ride = rideMatchingService.requestRide(dummyLocation, limit);
        assertTrue(RegisterService.getAvailableDrivers().isEmpty());

        rideMatchingService.completeRide(ride.getId());
        assertEquals(Ride.RideStatus.COMPLETED, ride.getStatus());
        assertFalse(RegisterService.getAvailableDrivers().isEmpty());
    }

    @Test
    void testCompleteRide_NotFound_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rideMatchingService.completeRide("non-existent-ride-id");
        });
        assertEquals("Ride not found", exception.getMessage());
    }

    @Test
    void testCompleteRide_AlreadyCompleted_ThrowsException() {
        Driver driver = new Driver("D1", "Alice", dummyLocation);
        int limit = 10;
        RegisterService.registerDriver(driver.getId(), driver);
        Ride ride = rideMatchingService.requestRide(dummyLocation, limit);
        rideMatchingService.completeRide(ride.getId());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rideMatchingService.completeRide(ride.getId());
        });
        assertEquals("Ride is already completed", exception.getMessage());
    }

    @Test
    void testConcurrentRideRequests_OnlyOneDriverAllocated() throws InterruptedException, ExecutionException {
        Driver driver = new Driver("D1", "Exclusive Driver", dummyLocation);
        int limit = 10;
        RegisterService.registerDriver(driver.getId(), driver);
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch successLatch = new CountDownLatch(1);
        CountDownLatch losersLatch = new CountDownLatch(threadCount - 1);
        List<Callable<Ride>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    Ride ride = rideMatchingService.requestRide(dummyLocation, limit);
                    successLatch.countDown();
                    return ride;
                } catch (Exception e) {
                    losersLatch.countDown();
                    throw e;
                }
            });
        }

        List<Future<Ride>> results = new ArrayList<>();
        for (Callable<Ride> task : tasks) {
            results.add(executor.submit(task));
        }

        boolean matchOccurred = successLatch.await(2, TimeUnit.SECONDS);
        assertTrue(matchOccurred);

        executor.shutdownNow(); // Force-interrupt the waiting threads looping in requestRide

        boolean losersInterrupted = losersLatch.await(2, TimeUnit.SECONDS);
        assertTrue(losersInterrupted);

        int successfulMatches = 0;
        int failedMatches = 0;

        for (Future<Ride> future : results) {
            try {
                Ride ride = future.get();
                if (ride != null)
                    successfulMatches++;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                boolean isInterrupted = (cause instanceof RuntimeException && cause.getMessage() != null
                        && cause.getMessage().contains("interrupted"));
                boolean isNoDriver = (cause instanceof IllegalStateException && cause.getMessage() != null
                        && cause.getMessage().contains("No available drivers"));
                if (isInterrupted || isNoDriver) {
                    failedMatches++;
                } else {
                    fail("Unexpected exception: " + cause);
                }
            }
        }
        assertEquals(1, successfulMatches, "Exactly one thread should successfully claim the driver");
        assertEquals(threadCount - 1, failedMatches,
                "The remaining threads should have stayed blocked/looping until interrupted");
        assertTrue(RegisterService.getAvailableDrivers().isEmpty());
    }
}