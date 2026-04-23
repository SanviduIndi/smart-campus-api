# Smart Campus API

A RESTful API built with Jersey for managing university rooms and sensors as part of the "Smart Campus" initiative.

---

## API Overview

The API follows REST principles and is structured around two core resources:

- **Rooms** — physical spaces on campus
- **Sensors** — hardware devices deployed inside rooms
- **Sensor Readings** — historical data logged by each sensor

Base URL: `http://localhost:8080/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1 | Discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{id} | Get a specific room |
| DELETE | /api/v1/rooms/{id} | Delete a room |
| GET | /api/v1/sensors | List all sensors (supports ?type= filter) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{id}/readings | Get readings for a sensor |
| POST | /api/v1/sensors/{id}/readings | Add a reading for a sensor |

---

## How to Build and Run

### Prerequisites
- Java 23
- Maven 3.6+

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/YOURUSERNAME/smart-campus-api.git
cd smart-campus-api
```

**2. Build the project**
```bash
mvn clean package
```

**3. Run the server**
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

**4. The server starts at:**
```
http://localhost:8080/api/v1
```

---

## Sample curl Commands

**1. Discovery endpoint**
```bash
curl -X GET http://localhost:8080/api/v1
```

**2. Get all rooms**
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

**3. Create a new room**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-01","name":"Main Hall","capacity":200}'
```

**4. Get all CO2 sensors (filtered)**
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

**5. Register a new sensor**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"OCC-001","type":"Occupancy","status":"ACTIVE","currentValue":0,"roomId":"LAB-101"}'
```

**6. Add a sensor reading**
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

**7. Get all readings for a sensor**
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**8. Delete a room (will fail if sensors exist)**
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/HALL-01
```
