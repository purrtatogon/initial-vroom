# Initial Vroom — Race Telemetry Simulator & Dashboard

*"In active development; expect rough edges."*

A full-stack telemetry playground that simulates head-to-head mountain pass battles and streams the results to a live dashboard. It borrows the mood of *Initial D*'s mountain passes and the always-on data obsession of endurance racing (think Le Mans radios), implemented with **Java Spring Boot**, **Angular**, and **MongoDB**. REST handles roster and battle commands; **STOMP over WebSocket** pushes ~20 Hz telemetry so the UI behaves like a real cluster reading the wire—not inventing physics in the browser.

**🔗 [Live demo (Render)*](https://initial-vroom-frontend.onrender.com)*

> **Deployment note:** Free-tier **Render** (expect **cold starts** after the app idles). **No accounts, passwords, or logins**—you use the flow below as soon as the page loads.

---

## How to Initial Vroom

Each run is a **head-to-head mountain pass battle**: you pick two cars from the roster, the **backend simulates the race** (single source of truth, like real telemetry), and the frontend **only displays** what the server streams—speed, RPM, gear, gap, elapsed time, plus an SVG pass map that tracks progress until someone finishes.

**Screenshots** below use a **1280px-wide** viewport. On the **Battle Picker**, **Stage 2** sits below the fold—scroll to browse both stages.

1. Open the app on the **Battle Picker** and assign **one car to each lane** (Stage 1 and Stage 2 grids are grouped; scroll if you need both).

INITIAL VROOM battle picker at first load: empty left and right lanes and the start of the Stage 1 car grid; Stage 2 is available below the fold.

1. Press **Start Battle**. That issues a REST start command; live updates arrive over **STOMP/WebSocket** at ~20 Hz.
2. Watch the **dual dashboard** and map until the simulation ends on its own.

INITIAL VROOM live race dashboard: paired speed and RPM gauges for two drivers, timing and gap readouts, and the SVG mountain pass map with both cars on the course.

1. Land on **Battle Results** for the winner, finishing gap, race time, and a stat compare between the two cars.

INITIAL VROOM battle results: winner summary, side-by-side car statistics, race duration, and finishing gap.

**💡 Exploration tip:** The loop injects light speed jitter—**rematch the same pair** and you might get a different photo finish.

---

## Project Background

This started as a junior-portfolio hunt that turned into a deep dive on **automotive dashboards**—clusters, ECU-style readouts, and live telemetry UX I barely understood at first. I wired that curiosity to **Initial D** (skill and setup over raw horsepower) and **Le Mans** (hours of radio and timing drama), and kept scope small enough to ship while still forcing **honest streaming**, **REST + WebSocket**, and data that had to survive a real schema.

**Angular** and **MongoDB** were deliberate bets: standalone components + RxJS on one side, document-shaped specs loaded with **OpenCSV** on the other—same “build it like it’s real” instinct as my other projects, just on asphalt instead of a merch table.

### 📊 Data realism & sourcing

I built the roster off **real cars**—the actual JDM machinery behind each *Initial D* matchup—using factory specs and chassis research, then nudged figures where the series clearly moves into swaps, turbos, or weight-saving mods. Those rows live in `**stage1_battle cars.csv*`* and `**stage2_battle cars.csv**` and load into MongoDB as `**Car**` documents (`backend/src/main/java/com/initialvroom/entity/Car.java`) via OpenCSV.

### 🎨 Design & accessibility

**Aesthetic:** a **"Midnight Run"** ECU homage—VT323, amber and green on black, dense readouts over decoration.

**Accessibility:** contrast-minded tokens, **44×44** interactive targets where it matters, visible `**:focus-visible`** affordances, and `**prefers-reduced-motion**` respected globally—same earnest bar I aim for elsewhere even while formal audits are still a growth edge.

---

## Architecture & Tech Stack

**Compose:** MongoDB, Spring Boot, and an nginx-built Angular bundle—the SPA talks to **REST** and `**/vroom-ws`**/`**/topic/race**` directly; nginx only serves static assets.

**Sim:** `**RaceSimulationService`** runs on the server (**50 ms** ticks → ~20 Hz), publishes `**BattleTelemetryDTO`** to `**/topic/race**`; REST starts/stops battles and loads cars from `**DataInitializer**` when MongoDB’s `cars` collection is empty.


| Layer        | Stack                                            |
| ------------ | ------------------------------------------------ |
| **Backend**  | Java 21, Spring Boot 3.2.0, MongoDB, OpenCSV 5.9 |
| **Frontend** | Angular 18.2 (standalone), RxJS, STOMP/SockJS    |
| **Ops**      | Docker Compose                                   |


---

## Features & Roadmap

No Figma phase—UI decisions hardened while coding.

### Dashboard & flow


| Status | Feature                                                        |
| ------ | -------------------------------------------------------------- |
| ✅      | Battle Picker with stage-grouped roster from MongoDB           |
| ✅      | Live dual telemetry dashboard (speed, RPM, gear, gap, elapsed) |
| ✅      | SVG mountain pass with sprites driven off progress             |
| ✅      | Post-race results (winner, stats, time, gap)                   |
| ✅      | REST + WebSocket composition (commands vs stream)              |


### Up next


| Status | Area                                                                                                                                                                                                                                                                      |
| ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 🚧     | **Assets** — richer car art beyond placeholders where it still looks thin                                                                                                                                                                                                 |
| 🚧     | **Accessibility** — keep tightening keyboard routes and screen-reader coverage                                                                                                                                                                                            |
| ⏳      | **Testing** — not implemented yet; next stretch is backend service tests (JUnit), Angular specs where they earn their keep, and automated a11y checks as I build confidence                                                                                               |
| ⏳      | **Replay / progression** — stored replays, unlock flow—ideas on the backlog                                                                                                                                                                                               |
| ⏳      | **Dashboard realism** — Rework on-screen layout to echo **real in-car clusters/HMIs** more closely, with **less emphasis** on the SVG mountain pass (maybe **hide** or **resize** it). Still studying reference instrumentation and the cleanest UX—nothing locked in yet |


---

## 🏃‍♂️ Getting Started

### Option A: Docker (recommended)

Requires **Docker Desktop**.

```bash
docker compose up --build
```

If your install still wires the hyphenated binary, use `docker-compose up --build` instead.


| Service                 | URL                                                                    |
| ----------------------- | ---------------------------------------------------------------------- |
| Angular app (nginx)     | [http://localhost:4200](http://localhost:4200)                         |
| Backend API / WebSocket | [http://localhost:8081](http://localhost:8081) (`/vroom-ws` for STOMP) |
| MongoDB                 | localhost:27017                                                        |


First build can take a few minutes. Stop with `docker compose down` (or `docker-compose down`).

On **first backend startup** with an **empty `cars` collection**, `DataInitializer` loads `**stage1_battle cars.csv`** and `**stage2_battle cars.csv**` from `backend/src/main/resources/data/`; if MongoDB already has cars, restarts **skip** re-seeding.

### Option B: Manual (for development)

Requires **Java 21**, **Maven**, **Node.js 20+**, and **MongoDB** on `localhost:27017`.

**1. Database** — ensure a `vroom` database is reachable per `backend/src/main/resources/application-local.properties`.

**2. Backend**

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**3. Frontend**

```bash
cd frontend
npm install
npm start
```


| Service            | URL                                            |
| ------------------ | ---------------------------------------------- |
| Angular dev server | [http://localhost:4200](http://localhost:4200) |
| Backend            | [http://localhost:8081](http://localhost:8081) |


Same `**DataInitializer**` behavior as Docker: CSV seed runs **once** when `cars` is empty; later runs keep whatever is already in MongoDB.

---

> **Built with the mindset that even “fake” telemetry should respect where the numbers come from—the server tells the story; the dashboard just listens.**
>
> *Thanks for reading — and for humoring a junior attempt to glue Spring Boot streams, midnight-dash glow, and imaginary mountain passes into one runnable lap. ◕⩊◕*

---

