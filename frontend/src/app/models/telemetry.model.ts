/**
 * Per-car data received from the WebSocket every 50ms.
 * progress (0–1) is the key field — it positions the car on the SVG track map.
 */
export interface Telemetry {
  carModelId: string;
  driverName: string;
  carDisplayName: string;
  driverTeam: string;
  currentSpeed: number;
  currentRpm: number;
  gear: number;
  progress: number;
  imageUrl: string;
}

/**
 * The full WebSocket message payload — matches BattleTelemetryDTO on the backend.
 * When finished is true, the race is over and we navigate to results.
 */
export interface BattleTelemetry {
  car1: Telemetry;
  car2: Telemetry;
  gap: number;
  elapsedMs: number;
  finished: boolean;
}
