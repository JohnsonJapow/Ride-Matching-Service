package org.core.models;

import java.util.concurrent.locks.ReentrantLock;

public class Driver {
    private final String id;
    private volatile String name;
    private volatile Location location;
    private volatile boolean isAvailable;
    private final ReentrantLock lock = new ReentrantLock();

    public Driver(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}