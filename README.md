# Ride Matching Service

A Java backend service that matches riders with the nearest available drivers in real time, with thread-safe concurrent request handling.

---

## Features

- Register and update driver name, availability and location
- Request a ride — automatically matched to the nearest available driver
- Complete a ride — driver becomes available again for new requests
- Query the X nearest available drivers from any location

---

## Requirements

- Java 21 or higher
- Gradle 8+ (or use the included Gradle wrapper)

---

## Setup

### 1. Clone the repository

```bash
mkdir new-folder
git clone https://github.com/JohnsonJapow/Ride-Matching-Service.git
cd ride-matching-service
```

### 2. Build the project

```bash
./gradlew build
```

### 3. Run the application

```bash
java -jar ./app/build/libs/app.jar
```

Or run directly via Maven:

```bash
./gradlew run
````

---

## Usage

The application runs as an interactive command-line menu:

```
===== Ride Matching System =====
1. Register Driver
2. Request Ride
3. Complete Ride
4. View Available Drivers (nearest X)
5. Exit
```

### 1. Register Driver

Registers a new driver or updates an existing driver's name and location. A re-registered driver is automatically marked as available.

**Prompts:**
- Driver ID (string)
- Driver Name (string)
- Location X coordinate (decimal)
- Location Y coordinate (decimal)

### 2. Request Ride

Finds and allocates the 10 nearest available driver to a pickup location. Returns the ride ID and matched driver details.

**Prompts:**
- Pickup location X coordinate (decimal)
- Pickup location Y coordinate (decimal)

**Example output:**
```
Ride created!
Ride ID: 3f2a1b4c-...
Driver name: Alice
Driver ID: driver-001
```

### 3. Complete Ride

Marks a ride as completed and releases the driver back into the available pool.

**Prompts:**
- Ride ID (from step 2)

### 4. View Available Drivers

Returns the X nearest available drivers to a given location, sorted by distance ascending, and by driver ID in ascending order, and then by driver ID (ascending) in case of a tie.

**Prompts:**
- Location X coordinate (decimal)
- Location Y coordinate (decimal)
- Number of drivers to return (positive integer)

---

## Running Tests

```bash
./gradlew test
```