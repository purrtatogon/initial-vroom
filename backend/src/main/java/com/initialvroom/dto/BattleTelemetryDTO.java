package com.initialvroom.dto;

/**
 * The full payload sent over WebSocket every tick (every 50ms).
 * Contains telemetry for both cars, the gap between them, elapsed time, and whether the race ended.
 * Jackson serializes this to JSON and STOMP pushes it to /topic/race.
 */
public class BattleTelemetryDTO {

    private TelemetryDTO car1;
    private TelemetryDTO car2;
    private double gap;       // distance between the two cars in meters
    private long elapsedMs;   // how long the race has been running
    private boolean finished; // true on the final tick when someone crosses the line

    public BattleTelemetryDTO() {
    }

    public BattleTelemetryDTO(TelemetryDTO car1, TelemetryDTO car2, double gap, long elapsedMs, boolean finished) {
        this.car1 = car1;
        this.car2 = car2;
        this.gap = gap;
        this.elapsedMs = elapsedMs;
        this.finished = finished;
    }

    public TelemetryDTO getCar1() {
        return car1;
    }

    public void setCar1(TelemetryDTO car1) {
        this.car1 = car1;
    }

    public TelemetryDTO getCar2() {
        return car2;
    }

    public void setCar2(TelemetryDTO car2) {
        this.car2 = car2;
    }

    public double getGap() {
        return gap;
    }

    public void setGap(double gap) {
        this.gap = gap;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
