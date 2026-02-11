# 🏎️ Initial Vroom

### A High-Performance Race Telemetry Simulator & Visualizer

**Initial Vroom** is a full-stack telemetry platform designed to simulate and visualize the performance of iconic mountain-pass racers. Inspired by the legendary battles on _Mt. Akina_, this project serves as a technical demonstration of real-time data streaming, containerized microservices, and specialized data modeling.

---

## 🛠️ The Stack

- **Frontend:** Angular 15+ (SCSS, RxJS for stream management)
- **Backend:** Java 17 / Spring Boot (Data Simulation Engine)
- **Database:** MongoDB (Time-series storage for race logs)
- **Communication:** STOMP over WebSockets for <100ms latency updates
- **DevOps:** Docker Compose (Production-ready containerization)

---

## 🐳 Docker Architecture

The application is fully containerized. Below are the active services and their port mappings:

| Service      | Container Name     | Internal Port | External Port | Description                        |
| :----------- | :----------------- | :------------ | :------------ | :--------------------------------- |
| **Frontend** | `front`            | 4200          | **4200**      | Angular Dashboard UI               |
| **Backend**  | `back`             | 8081          | **8081**      | Spring Boot API & WebSocket Stream |
| **Database** | `db-mongo-initial` | 27017         | **27017**     | MongoDB Instance                   |

---

## 🎨 Aesthetic & Assets

The project uses a **stylized 2D approach** to honor its manga roots:

- **Visuals:** High-quality 2D cut-outs of vehicles (Current: Takumi's AE86; others currently use designated placeholders).
- **The "Midnight Run" Vision:** The UI is being evolved toward a **90s Tuner Laptop** aesthetic—think high-contrast amber/green text on black backgrounds, resembling a standalone ECU hooked up to a dashboard during a night run.

> **Design Note:** The interface is a deliberate stylistic choice to mimic early 90s standalone engine management systems.

---

## 🚦 Current Features

- **Race Simulation Engine:** Generates correlated physics data (RPM, Gear, Speed, Tire Temp) for Stage 1 and Stage 2 battle cars.
- **Live Dashboard:** Real-time gauges and performance metrics pushed via WebSockets.
- **Containerized Workflow:** Launch the entire stack (DB, API, UI) with a single `docker-compose up`.

---

## 🗺️ Roadmap & Future Improvements

I've started building a dashboard but maybe I'll end up building a game engine!

- [ ] **SVG Pass Maps:** Implementing dynamic SVG visualizations for car positions on the _Usui_ or _Akina_ mountain passes.
- [ ] **"Tuner" UI Overhaul:** Switching to monospaced fonts (`VT323`) and terminal-style data grids for a raw, technical feel.
- [ ] **Race Replay Mode:** A "VCR-style" feature to fetch historical data from MongoDB and replay previous runs frame-by-frame.
- [ ] **Dogfight Mode:** Multi-car battles on a single WebSocket channel for head-to-head performance tracking.
- [ ] **Progression System:** Introducing **Initial D Points (IDP)** and a car unlocking system based on simulation performance.

---

## 🚀 Quick Start

1.  **Clone the repository**

    ```bash
    git clone [https://github.com/s0raia/initial-vroom.git](https://github.com/s0raia/initial-vroom.git)
    cd initial-vroom
    ```

2.  **Start the Application**
    Ensure Docker Desktop is running, then build and launch the containers:

    ```bash
    docker-compose up --build
    ```

3.  **Access the Dashboard**
    Open your browser and navigate to:
    `http://localhost:4200/dashboard`

<p align="center">◕⩊◕<br>
<em>Thanks for checking out Initial Vroom!</em>
</p>
