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

## Report: Question Answers

**Part 1.1 — JAX-RS Resource Lifecycle**

By default JAX-RS creates a brand new instance of a resource class for every single request that comes in. This means you can't just store your rooms list as a field inside the resource class, it'd get wiped after each request.

To get around this I created a separate DataStore class that follows the Singleton pattern. Every resource class calls DataStore.getInstance() which always returns the same object, so the data actually persists between requests. I also used ConcurrentHashMap instead of a regular HashMap because multiple requests can arrive at the same time, a regular HashMap isn't thread safe and concurrent writes can corrupt it.

---

### Part 1.2 — HATEOAS and Hypermedia

HATEOAS basically means your API responses include links telling the client what they can do next, rather than just dumping raw data. 
So instead of just returning a room object, you'd also include something like a link to its sensors or a link to delete it.
The benefit for client developers is they don't need to hardcode URLs or constantly check documentation to figure out what endpoints exist. The API kind of guides them. It also means if you ever change your URL structure, clients that follow the links rather than hardcoding paths won't break.

---

### Part 2.1 — Returning IDs vs Full Objects

If you only return IDs in the rooms list, the client has to then make a separate request for every single room to get the details, this is called the N+1 problem and gets very slow with large datasets.
Returning full objects means one request gets everything. The payload is bigger but in practice it's much faster since room objects are fairly small. For this API returning full objects makes more sense.

---

### Part 2.2 — DELETE Idempotency

YYes it is idempotent in terms of server state. Whether you call DELETE on a room once or five times, the end result is the same, the room doesn't exist. The difference is what you get back. First successful call returns 204, any repeat calls return 404 since the room is already gone. The HTTP spec says idempotency is about the effect on the server, not the response code, so this is correct behaviour.

---

### Part 3.1 — @Consumes(MediaType.APPLICATION_JSON)

This annotation tells JAX-RS that this endpoint only accepts JSON. If a client sends a request with Content-Type: text/plain or application/xml, JAX-RS rejects it automatically before it even gets to my code and sends back a 415 Unsupported Media Type. I don't have to write any manual checks for it which is quite useful.

---

### Part 3.2 — Query Parameters vs Path Segments for Filtering

/sensors?type=CO2 makes it clear that type is just a filter on the existing collection, the endpoint /sensors still works fine without it.
If you put it in the path like /sensors/type/CO2 it implies that's an actual separate resource which is semantically wrong. It's just a filter. Query params also stack nicely, you could do ?type=CO2&status=ACTIVE without it getting messy. The general rule in REST is: path params identify a resource, query params filter or modify it.

---

### Part 4.1 — Sub-Resource Locator Pattern

Instead of cramming every single endpoint into one massive class, the sub-resource locator pattern lets you hand off to a separate class. So SensorResource deals with /sensors stuff and just passes /sensors/{id}/readings over to SensorReadingResource.
The big advantage is each class stays focused on one thing. If the readings logic gets more complex later — like adding pagination or date range filters, you just work in SensorReadingResource without touching anything else. In a big real-world API having everything in one file would be a nightmare to maintain.
---

### Part 5.2 — HTTP 422 vs 404 for Missing References

404 means the URL doesn't exist. But when you POST a sensor with a bad roomId, the URL /api/v1/sensors is completely valid — the problem is inside the request body, not the URL.
422 is more honest here because it's saying "I understood your request fine, but the data inside it doesn't make sense logically." Returning 404 would make the client think the endpoint itself is missing which is just confusing and misleading.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to API consumers is a significant security vulnerability for several reasons:

1. Technology fingerprinting — the trace reveals the exact framework, library versions, and Java version in use, allowing attackers to look up known CVEs for those specific versions.
2. Internal path disclosure — file paths in the trace reveal the server's directory structure and package naming conventions.
3. Logic disclosure — the call stack reveals internal class names, method names, and the exact line of code that failed, giving attackers a map of the application's internals to target.
4. Easier exploit development — knowing exactly where and why something failed dramatically reduces the effort needed to craft a targeted attack.

The Global Exception Mapper solves this by catching all unexpected errors and returning a generic 500 message that reveals nothing about internal implementation.

---

### Part 5.5 — JAX-RS Filters for Cross-Cutting Concerns

If you manually add logging to every resource method you end up with the same code copy-pasted everywhere. If you ever need to change the log format you have to hunt through every single file. Filters solve this by running automatically on every request and response at the framework level without touching the resource classes at all. The resource classes stay clean and focused on actual business logic, and logging behaviour is controlled from one place.
