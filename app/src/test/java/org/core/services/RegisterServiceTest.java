package org.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.core.models.Driver;
import org.core.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegisterServiceTest {

    @BeforeEach
    void setUp() {
        RegisterService.clearAll();
    }

    @Test
    void testRegisterNewDriver() {
        Location location = new Location("10", "20");
        Driver driverBeforeRegistered = new Driver("D1", "Alice", location);

        RegisterService.registerDriver(driverBeforeRegistered.getId(), driverBeforeRegistered);

        Driver driverAfterRegistered = RegisterService.getDriver(driverBeforeRegistered.getId()).get();

        assertNotNull(driverAfterRegistered);
        assertEquals(driverBeforeRegistered.getId(), driverAfterRegistered.getId());
        assertEquals(driverBeforeRegistered.getName(), driverAfterRegistered.getName());
        assertEquals(driverBeforeRegistered.getLocation(), driverAfterRegistered.getLocation());
    }

    @Test
    void testRegisterExistingDriver_UpdatesLocation() {
        String id = "D1";
        Location location1 = new Location("10", "20");
        Location location2 = new Location("30", "40");
        Driver originalDriver = new Driver(id, "Alice", location1);
        RegisterService.registerDriver(originalDriver.getId(), originalDriver);
        Driver updatedDriver = new Driver(id, "Bob", location2);
        RegisterService.registerDriver(updatedDriver.getId(), updatedDriver);

        Driver latestRecord = RegisterService.getDriver(id).get();

        assertEquals(latestRecord.getLocation(), updatedDriver.getLocation());
        assertEquals(latestRecord.getName(), updatedDriver.getName());
    }

    @Test
    void testConcurrentRegistration_SameDriverId() throws Exception {
        int threadCount = 20;
        System.out.println(RegisterService.getAvailableDrivers().size());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            final int index = i;

            futures.add(executor.submit(() -> {
                startLatch.await();

                RegisterService.registerDriver(
                        "D1",
                        new Driver("D1", "Alice",
                                new Location(String.valueOf(index), String.valueOf(index))));

                return null;
            }));
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        List<Driver> drivers = RegisterService.getAvailableDrivers();
        assertEquals(
                1,
                drivers.size(),
                "Only one driver should exist for the same ID");

        Driver driver = RegisterService.getDriver("D1").get();

        assertNotNull(driver);
        assertEquals("D1", driver.getId());
    }

    @Test
    void testConcurrentRegistration_DifferentDriverIds() throws Exception {
        int threadCount = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            final int index = i;

            futures.add(executor.submit(() -> {
                startLatch.await();

                RegisterService.registerDriver(
                        "D" + index,
                        new Driver("D" + index, "Driver" + index,
                                new Location(String.valueOf(index), String.valueOf(index))));

                return null;
            }));
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        List<Driver> drivers = CalculateService.getNearestAvailableDrivers(RegisterService.getAvailableDrivers(),
                new Location("0", "0"), threadCount);
        assertEquals(threadCount, drivers.size());

        for (int i = 0; i < threadCount; i++) {
            assertTrue(
                    drivers.get(i).getId().equals("D" + i));
        }
    }
}