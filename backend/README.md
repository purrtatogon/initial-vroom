# Backend -- Spring Boot API and Simulation Engine

This is the Java 21 / Spring Boot 3.2 backend for Initial Vroom. It serves as a REST API for car data and battle management, a STOMP WebSocket broker for real-time telemetry, and a server-side race simulation engine. MongoDB stores the car specifications, loaded from CSV seed files on startup.

---

## Tech Stack

| Technology | Version | Role |
|:-----------|:--------|:-----|
| Java | 21 | Language runtime |
| Spring Boot | 3.2.0 | Application framework (web, data, websocket, scheduling) |
| Spring Data MongoDB | (managed by Boot) | Repository pattern for MongoDB access |
| Spring WebSocket | (managed by Boot) | STOMP message broker over SockJS |
| OpenCSV | 5.9 | CSV-to-Java-bean mapping with annotation-based column binding |
| MongoDB | latest (Docker) | Document database for the `cars` collection |
| Maven | 3.9 | Build tool and dependency management |

---

## Package Structure

```
src/main/java/com/initialvroom/
├── InitialVroomApplication.java    # Entry point — @SpringBootApplication + @EnableScheduling
│
├── config/
│   ├── DataInitializer.java        # CommandLineRunner — loads CSV seed data into MongoDB on startup
│   ├── WebSocketConfig.java        # STOMP broker config — /topic prefix, /vroom-ws endpoint
│   └── BooleanConverter.java       # Custom OpenCSV converter for "TRUE"/"FALSE" strings
│
├── entity/
│   └── Car.java                    # MongoDB @Document — 15 fields mapped from CSV columns
│
├── repository/
│   └── CarRepository.java          # MongoRepository<Car, String> with derived query findByStageId
│
├── dto/
│   ├── BattleRequest.java          # Request body for POST /api/battles { car1Id, car2Id }
│   ├── TelemetryDTO.java           # Per-car telemetry snapshot (speed, rpm, gear, progress)
│   └── BattleTelemetryDTO.java     # Full WebSocket payload (car1, car2, gap, elapsed, finished)
│
├── controller/
│   ├── CarController.java          # REST: GET /api/cars, GET /api/cars?stageId=X
│   └── BattleController.java       # REST: POST /api/battles, POST /api/battles/stop
│
└── service/
    └── RaceSimulationService.java  # The simulation engine — @Scheduled tick loop, speed model, WebSocket broadcast
```

### Resource Files

```
src/main/resources/
├── application.properties              # Shared defaults (app name, port, database name)
├── application-local.properties        # Local dev profile (localhost MongoDB)
├── application-docker.properties       # Docker Compose profile (db:27017)
├── application-prod.properties         # Production profile (MongoDB URI from env var, e.g. Atlas)
└── data/
    ├── stage1_battle cars.csv          # 7 Stage 1 cars (15 columns each)
    └── stage2_battle cars.csv          # 7 Stage 2 cars (15 columns each)
```

---

## How It Works

### 1. Data Loading: CSV to MongoDB

When the application starts, `DataInitializer` (a `CommandLineRunner`) checks if the `cars` collection in MongoDB is empty. If it is, it reads both CSV files from the classpath, parses them into `Car` objects using OpenCSV, and batch-inserts all 14 documents with `saveAll()`.

**Why CSV?** The car data is version-controlled alongside the code. No external API, no scraping, no authentication. Adding a car means adding one CSV row and restarting.

**Why CommandLineRunner?** It runs once after the Spring context is ready. The `count() > 0` check makes it idempotent -- restarting the app does not create duplicates.

**OpenCSV Mapping:**
- `@CsvBindByName(column = "car_hp")` on the `Car` entity maps the CSV column `car_hp` to the Java field `carHp`.
- `@CsvCustomBindByName` with a custom `BooleanConverter` handles the `has_speed_chime` column, which contains `"TRUE"` / `"FALSE"` strings that OpenCSV does not parse as booleans by default.

**The `@Id` on `carModelId`:**
The CSV's `car_model_id` column (e.g., `ae86_takumi_stg1_stock`) becomes the MongoDB `_id`. This is a natural key -- human-readable and meaningful, unlike a random ObjectId. The trade-off: the ID must be truly unique and immutable in the CSV.

### 2. REST API: Serving Car Data

`CarController` exposes two GET endpoints:

| Endpoint | Method | Return |
|:---------|:-------|:-------|
| `/api/cars` | `findAll()` | All 14 cars |
| `/api/cars?stageId=Stage 1` | `findByStageId("Stage 1")` | Cars for that stage |

`CarRepository` extends `MongoRepository<Car, String>`, which provides all standard CRUD methods. The custom `findByStageId` method is a Spring Data **derived query** -- Spring auto-generates the MongoDB query from the method name.

**JSON serialization**: Jackson (included with Spring Boot) serializes the `Car` entity to JSON with camelCase keys. The frontend's TypeScript `Car` interface uses the same casing, so no mapping layer is needed.

### 3. Battle Control: REST Commands

`BattleController` handles two POST endpoints:

| Endpoint | Action |
|:---------|:-------|
| `POST /api/battles` | Validates both car IDs in MongoDB, then calls `startBattle()` on the simulation service |
| `POST /api/battles/stop` | Calls `stopBattle()` to halt the simulation loop |

**Why POST and not GET?** These operations change server state (starting/stopping a race). REST convention: GET is for reading, POST is for state changes.

**Error handling**: If either car ID does not exist in MongoDB, the endpoint returns HTTP 400 with a message. The frontend displays this in an error banner.

### 4. The Race Simulation Engine

`RaceSimulationService` is the most complex class in the project. It simulates a two-car race in real time.

#### The Tick Loop

```java
@Scheduled(fixedRate = 50)
public void tick() { ... }
```

Every 50 milliseconds (20 times per second), the `tick()` method:

1. **Computes new speed** for each car based on its stats and current position.
2. **Updates distance**: `distance += (speed / 3.6) * 0.05` (km/h to m/s, times tick duration).
3. **Checks for finish**: if either car reaches 5000 meters, `finished = true`.
4. **Builds telemetry DTOs** with progress (0.0-1.0), speed, RPM, and gear.
5. **Broadcasts** the `BattleTelemetryDTO` to all WebSocket subscribers via `SimpMessagingTemplate.convertAndSend("/topic/race", battle)`.

**Why @Scheduled?** It is the idiomatic Spring way to run periodic tasks. Uses a managed thread pool, handles exceptions gracefully, requires zero thread management code.

**Why 50ms?** 20 updates per second is smooth enough for visual animation without excessive CPU or network cost. Higher (60Hz) triples load with diminishing visual returns.

#### Speed Model

The simulation is built around **power-to-weight ratio**, the single most important performance metric in real motorsport:

```java
double pwRatio = (double) car.getCarHp() / car.getWeightKg();
double maxSpeed = 80.0 + pwRatio * 300.0;         // straight-line top speed
double acceleration = pwRatio * 120.0;              // acceleration rate
double hairpinSpeed = 40.0 + pwRatio * 60.0;       // cornering speed
```

**Drivetrain bonuses**: `4WD` gets 8% more speed through hairpin zones (simulates mechanical grip). `Turbo` / `Twin Turbo` gets 4% more speed on straights.

**Braking**: when entering a hairpin, braking force is 2.5x the acceleration rate. This creates the characteristic "hard brake into a corner, slow through the apex" pattern visible on the speedometer.

**Jitter**: `(random.nextDouble() - 0.5) * 4.0` adds +/- 2 km/h per tick. Small enough that faster cars still win most of the time, but large enough that close matchups have uncertain outcomes.

#### Hairpin Zones

Five zones defined as progress intervals along the 5km track:

| Zone | Start | End |
|:-----|:------|:----|
| H1 | 15% | 20% |
| H2 | 30% | 35% |
| H3 | 45% | 50% |
| H4 | 60% | 65% |
| H5 | 78% | 83% |

Inspired by Akina's five consecutive hairpins. These are abstract positions on the normalized track length -- they do not correspond to the SVG path curves on the frontend (those are independent visual elements).

#### Gear and RPM (Display Model)

**Gear** is a step function of speed: 0-30 km/h = 1st, 30-60 = 2nd, 60-100 = 3rd, 100-140 = 4th, 140+ = 5th. Not a transmission simulation -- just cosmetic for the dashboard gauges.

**RPM** interpolates linearly within each gear's speed band, from idle (1000 RPM) to the car's `redlineRpm`. Cars with higher redlines show a wider sweep on the tachometer, which is visually accurate even though RPM does not feed back into the speed calculation.

#### Finish and Winner

When either car's distance reaches 5000m, both are clamped so progress never exceeds 1.0. The `finished: true` flag is included in that tick's broadcast, then `battleActive` is set to `false` to stop the loop.

**The winner is not explicitly sent to the frontend.** The backend logs the winner server-side, but the DTO only includes final progress values. The frontend determines the winner by comparing `car1.progress` vs `car2.progress`. This keeps the DTO simple.

### 5. WebSocket: STOMP over SockJS

`WebSocketConfig` sets up the STOMP message broker:

- **Simple broker** on `/topic` -- an in-memory broker, no external message queue needed.
- **SockJS endpoint** at `/vroom-ws` -- provides fallback transport for environments where raw WebSocket is blocked (corporate proxies, certain mobile networks).
- **`setAllowedOriginPatterns("*")`** -- allows cross-origin connections from any frontend URL (localhost in dev, hosted SPA in prod).

The simulation service uses `SimpMessagingTemplate` to publish to `/topic/race`. Any connected STOMP client subscribed to that topic receives the `BattleTelemetryDTO` every 50ms.

---

## How the Backend Connects to the Frontend

### REST (consumed by Angular's HttpClient)

The frontend's `CarService` calls `GET /api/cars` to populate the battle picker. The `BattleService` calls `POST /api/battles` with `{ car1Id, car2Id }` to start a race and `POST /api/battles/stop` to abort.

The backend URL comes from Angular environment files (`environment.ts` for dev, `environment.prod.ts` for production). The browser calls the backend directly — no proxy layer.

### WebSocket (consumed by @stomp/stompjs + sockjs-client)

The frontend's `TelemetryService` connects to the backend's `/vroom-ws` endpoint directly using SockJS, subscribes to `/topic/race`, and pushes each incoming JSON message into an RxJS `Subject`. The dashboard component subscribes to this observable and updates the UI on every tick. Cross-origin WebSocket is allowed by `setAllowedOriginPatterns("*")` in `WebSocketConfig`.

### Data Contract

Jackson serializes Java fields to JSON with camelCase keys (matching Java naming conventions). The frontend's TypeScript interfaces use the same field names:

| Java Field | JSON Key | TypeScript Field |
|:-----------|:---------|:-----------------|
| `carModelId` | `carModelId` | `carModelId` |
| `currentSpeed` | `currentSpeed` | `currentSpeed` |
| `isFinished()` (boolean getter) | `finished` | `finished` |

No transformation or adapter layer exists. If a field is added to the Java entity or DTO, the corresponding TypeScript interface must be updated to match.

---

## Configuration

### Spring Profiles

The backend uses three environment profiles:

| Profile | Activated by | MongoDB connection |
|---------|-------------|-------------------|
| **local** | `mvn spring-boot:run -Dspring-boot.run.profiles=local` | `mongodb://localhost:27017/vroom` |
| **docker** | `SPRING_PROFILES_ACTIVE: docker` in docker-compose.yml | `mongodb://db:27017/vroom` (Docker DNS) |
| **prod** | `SPRING_PROFILES_ACTIVE=prod` on the host (e.g. Render) | `${SPRING_DATA_MONGODB_URI}` (e.g. Atlas from env/secrets) |

`application.properties` holds shared defaults (app name, port 8081, database name `vroom`).

---

## Build

### With Docker (recommended)

From the project root:

```bash
docker-compose up --build
```

If you use Docker Compose V2, run `docker compose up --build` instead. To stop the stack from the project root, use `docker-compose down` or `docker compose down`.

The backend Dockerfile uses a multi-stage build:
1. **Build stage** (`maven:3.9-eclipse-temurin-21-alpine`): downloads dependencies, compiles, and packages the JAR.
2. **Run stage** (`eclipse-temurin:21-jre-alpine`): copies just the JAR and runs it. No Maven, no source code in the final image.

The dependency download (`mvn dependency:go-offline`) is in a separate layer from the source compilation. This means rebuilding after a code change only recompiles -- it does not re-download all dependencies.

### Local Development

Requires Java 21, Maven, and MongoDB listening on `localhost:27017`:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The backend starts on `http://localhost:8081`. The `local` profile sets `spring.data.mongodb.uri` to `mongodb://localhost:27017/vroom` (see `application-local.properties`). Verify it is working:

```bash
curl http://localhost:8081/api/cars | jq
```

---

## Key Backend Decisions

| Decision | Reasoning |
|:---------|:----------|
| **MongoDB over PostgreSQL** | Car data is flat and self-contained (no joins, no relationships). Document model matches JSON shape. First-time learning goal. |
| **CSV seed files over an external API** | Portable, version-controlled, human-readable. No runtime dependency on third-party services. |
| **Natural key (`carModelId`) over ObjectId** | Readable IDs like `ae86_takumi_stg1_stock` make debugging and querying easier. |
| **Server-side simulation over client-side** | The server is the single source of truth. Mirrors real telemetry architecture where dashboards consume, not generate, data. |
| **`@Scheduled` over manual threading** | Idiomatic Spring, managed thread pool, no boilerplate. |
| **STOMP over raw WebSocket** | Topic-based pub/sub out of the box, Spring first-class support, SockJS fallback. |
| **Simplified physics (power-to-weight) over full engine model** | Plausible telemetry data without the complexity of modeling gear ratios, tire grip, or aerodynamics. Scope-appropriate for a dashboard project. |
| **Cosmetic gear/RPM model** | Produces visually correct dashboard readouts without feeding back into the speed calculation. |
| **OpenCSV over Apache Commons CSV** | Annotation-based binding (`@CsvBindByName`) is cleaner than manual column-index mapping. |
