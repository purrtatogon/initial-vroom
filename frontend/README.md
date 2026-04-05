# Frontend -- Angular Dashboard UI

This is the Angular 18 frontend for Initial Vroom. It renders the battle picker, the live telemetry dashboard, the SVG track map, and the post-race results screen. It connects to the Spring Boot backend via REST (for car data and battle commands) and STOMP-over-SockJS WebSocket (for real-time telemetry at 20Hz).

---

## Tech Stack

| Technology | Version | Role |
|:-----------|:--------|:-----|
| Angular | 18.2 | Standalone components, lazy-loaded routes, RxJS observables |
| TypeScript | 5.5 | Strict typing for all models, services, and components |
| RxJS | 7.8 | Observable streams for WebSocket telemetry data |
| @stomp/stompjs | 7.3 | STOMP client for subscribing to `/topic/race` |
| sockjs-client | 1.6 | WebSocket transport layer with fallback for blocked environments |
| nginx | alpine | Production server; serves static files and proxies API/WebSocket to the backend |

---

## File Structure

```
src/app/
├── app.component.ts           # Root shell — just a <router-outlet>
├── app.config.ts              # Standalone bootstrap config (provideRouter, provideHttpClient)
├── app.routes.ts              # Three lazy-loaded routes: /battle, /dashboard, /results
│
├── models/
│   ├── car.model.ts           # Car interface — mirrors the Java Car entity field-for-field
│   └── telemetry.model.ts     # Telemetry + BattleTelemetry — mirrors the backend DTOs
│
├── services/
│   ├── car.service.ts         # GET /api/cars — fetches the car roster from the backend
│   ├── battle.service.ts      # POST /api/battles — starts and stops races
│   ├── telemetry.service.ts   # STOMP/SockJS client — subscribes to /topic/race, exposes telemetry$
│   └── battle-result.service.ts  # In-memory singleton — holds the final telemetry snapshot between routes
│
├── battle-picker/             # First screen: browse cars by stage, select two, start a battle
│   ├── battle-picker.component.ts
│   ├── battle-picker.component.html
│   └── battle-picker.component.css
│
├── dashboard/                 # Live race: telemetry panels + track map + status bar
│   ├── dashboard.component.ts
│   ├── dashboard.component.html
│   └── dashboard.component.css
│
├── battle-results/            # Post-race: winner card, stat comparison, race time, gap
│   ├── battle-results.component.ts
│   ├── battle-results.component.html
│   └── battle-results.component.css
│
└── track-map/                 # SVG mountain pass with animated car sprites
    ├── track-map.component.ts
    ├── track-map.component.html
    └── track-map.component.css
```

---

## User Flow

The app has three screens connected by Angular's router:

```
/battle (default) ──► /dashboard ──► /results
      ▲                    │              │
      │                    │ (abort)      │ (rematch / back)
      └────────────────────┘──────────────┘
```

1. **Battle Picker** (`/battle`) -- The landing page. Fetches all 14 cars via `GET /api/cars`, groups them by `stageId` client-side, and displays them in stage sections. The user clicks cars to fill the Left Lane and Right Lane slots. Clicking **Start Battle** sends `POST /api/battles { car1Id, car2Id }` and navigates to `/dashboard`.

2. **Dashboard** (`/dashboard`) -- Connects to the WebSocket on mount. The `TelemetryService` subscribes to `/topic/race` and pushes `BattleTelemetry` objects into a `telemetry$` observable. The component updates speed, RPM, and gear gauges, and passes `progress` values (0-1) to the `TrackMapComponent`. When `finished` is true, it stores the final snapshot in `BattleResultService` and navigates to `/results` after a 1.5-second delay.

3. **Results** (`/results`) -- Reads the stored `BattleTelemetry` from `BattleResultService`. If nothing is stored (e.g., direct URL access), it redirects to `/battle`. Determines the winner by comparing `progress` values. Shows a winner card, a side-by-side stat grid, race time, and gap distance.

---

## How the Frontend Connects to the Backend

### REST (HTTP)

The frontend makes standard HTTP calls using Angular's `HttpClient`. In the Dockerized deployment, nginx proxies these to the backend:

| Service | Method | Backend Endpoint | Purpose |
|:--------|:-------|:-----------------|:--------|
| `CarService` | `GET` | `/api/cars` | Fetch the full car roster on the battle picker |
| `CarService` | `GET` | `/api/cars?stageId=X` | Fetch cars by stage (available, not currently used) |
| `BattleService` | `POST` | `/api/battles` | Start a race with two car IDs |
| `BattleService` | `POST` | `/api/battles/stop` | Abort the current race |

The base URL is just `/api/...` (relative). In Docker, nginx rewrites this to `http://backend:8081/api/...`. For local dev, you would use Angular's `proxy.conf.json` to forward to `localhost:8081`.

### WebSocket (STOMP/SockJS)

The `TelemetryService` uses `@stomp/stompjs` with `sockjs-client` as the transport:

1. On the dashboard mount, `connect()` creates a STOMP `Client` that connects to `/vroom-ws` (the backend's SockJS endpoint).
2. On connection, it subscribes to `/topic/race` -- the topic the backend publishes telemetry to every 50ms.
3. Each incoming message is parsed as `BattleTelemetry` and pushed into a `Subject`, exposed as `telemetry$`.
4. The dashboard component subscribes to `telemetry$` to update all UI bindings.
5. On navigation away (abort or race finish), `disconnect()` deactivates the client.

nginx proxies `/vroom-ws/` with the `Upgrade` and `Connection` headers required for the WebSocket handshake. Without these, the connection fails with a 403.

### Data Model Alignment

The TypeScript interfaces in `models/` mirror the Java entities and DTOs exactly:

| Frontend (TypeScript) | Backend (Java) | Serialization |
|:----------------------|:---------------|:--------------|
| `Car` interface | `Car` entity | Jackson camelCase JSON = TypeScript camelCase fields |
| `Telemetry` interface | `TelemetryDTO` | Jackson camelCase JSON |
| `BattleTelemetry` interface | `BattleTelemetryDTO` | Jackson camelCase JSON |

No manual mapping or transformation is needed on either side. Adding a field to the Java entity means adding the same field to the TypeScript interface.

---

## Visual Design: The "Midnight Run" Aesthetic

The UI is not generically dark-themed -- it is a deliberate homage to **early 90s standalone engine management systems**, the kind of monochrome CRT screens you would find in a tuner car's dashboard during a night run.

### Design Tokens

All visual constants are defined as CSS custom properties in `src/styles.css`:

| Token | Value | Purpose |
|:------|:------|:--------|
| `--color-bg` | `#0a0a0a` | Near-black background |
| `--color-panel` | `#111111` | Slightly lighter panel surfaces |
| `--color-amber` | `#ff9100` | Primary accent (headers, active states, amber gauges) |
| `--color-green` | `#00ff88` | Secondary accent (speed values, car 1 highlights) |
| `--color-red` | `#ff6b6b` | Error states and abort button (7.1:1 contrast) |
| `--color-text-primary` | `#e0e0e0` | Main body text |
| `--color-text-secondary` | `#b0b0b0` | Labels and secondary info (8.7:1 contrast) |
| `--color-text-dim` | `#a0a0a0` | Muted labels like gauge units (7.2:1 contrast) |
| `--font-display` | `'VT323', monospace` | Retro monospace for headers, data readouts, and labels |
| `--font-body` | System font stack | Clean sans-serif for body text |

### Typography

- **VT323** (Google Fonts) is used for all headers, gauge values, labels, and data readouts. It mimics the pixel-perfect text of old ECU monitors.
- **System font stack** (`-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, ...`) is used for body text like driver names and car descriptions.

### WCAG AAA Accessibility

The project targets **WCAG 2.2 Level AAA** compliance:

- **7:1 contrast ratio** for all normal-sized text against the dark background.
- **4.5:1 contrast ratio** for large text (18px+ or 14px+ bold).
- **3:1 contrast ratio** for non-text UI components (borders, dividers).
- **44x44px minimum** touch/click target size on all interactive elements (buttons, car cards).
- **Focus-visible states** on every interactive control using a 3px amber box-shadow ring.
- **`prefers-reduced-motion`** media query kills all animations and transitions globally.
- **Semantic HTML**: single `<h1>` per page, headings in order, `role` and `aria-label` attributes on interactive and landmark elements.
- **No reliance on color alone**: error states use an icon + text, not just red color. Selected car slots use border changes visible without color perception.

### The SVG Track Map

The track map (`track-map.component`) is a hand-drawn SVG mountain pass:

- **Contour lines**: five concentric ellipses with faint amber strokes create the topographic mountain feel.
- **Road surface**: a wide dark stroke (`stroke-width: 44`) drawn behind the thinner racing line.
- **Racing line**: a `#2a2a2a` path with an amber drop-shadow glow. This is the `<path>` element referenced by `@ViewChild` for `getPointAtLength()`.
- **Car sprites**: positioned at `progress * pathLength` along the path. The `getTravelAngle()` method samples two nearby points and computes the tangent angle with `atan2`, so cars face the direction of travel.
- **Horizontal flip**: `scale(-1, 1)` in the transform because the car sprite images face left by default.
- **50ms CSS transition** on car icons matches the backend tick rate for smooth visual movement.
- **Hairpin labels** (H1-H6) are visual reference markers. They do not correspond to the backend's hairpin zones -- the simulation and SVG are independent abstractions.

---

## Build and Development

### Development server

```bash
npm install
npm start
```

Runs on `http://localhost:4200`. Requires the backend running on `localhost:8081` (or a `proxy.conf.json` to forward `/api/` requests).

### Production build

```bash
npm run build
```

Output goes to `dist/frontend/browser/`. In Docker, this is copied into the nginx container.

### Docker build

The Dockerfile uses a multi-stage build:
1. **Stage 1** (`node:20-alpine`): installs dependencies with `npm ci` and compiles the Angular app.
2. **Stage 2** (`nginx:alpine`): copies the compiled static files and the custom `nginx.conf` into the nginx image.

The nginx config handles three responsibilities:
- Serves the Angular SPA with `try_files` fallback to `index.html` (required for client-side routing).
- Proxies `/api/` to `http://backend:8081/api/`.
- Proxies `/vroom-ws/` to `http://backend:8081/vroom-ws/` with WebSocket upgrade headers.

---

## Key Frontend Decisions

| Decision | Reasoning |
|:---------|:----------|
| **Standalone components** (no NgModule) | Angular 18 best practice; simpler, each component declares its own imports |
| **Lazy-loaded routes** | Each screen's code is only downloaded when navigated to; better initial load |
| **Client-side stage grouping** | With only 14 cars, fetching all at once and grouping in memory is simpler than two API calls |
| **In-memory result passing** (not localStorage) | Race results are ephemeral; the app is about the live experience, not historical data |
| **1.5s delay before results navigation** | Lets the user see the final dashboard state before the screen transitions |
| **CSS custom properties** (not Tailwind, not Angular Material) | Full control over the retro aesthetic; no framework fighting the theme |
| **`Subject` + `asObservable()`** for telemetry | Clean RxJS pattern: the service produces, components consume, no leaky abstractions |
