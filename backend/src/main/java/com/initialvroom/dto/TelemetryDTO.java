package com.initialvroom.dto;

/**
 * Per-car telemetry snapshot sent every tick.
 * progress is 0.0 to 1.0 — the frontend uses it with SVG getPointAtLength()
 * to position the car sprite on the track map. Much cleaner than sending pixel coords.
 */
public class TelemetryDTO {

    private String carModelId;
    private String driverName;
    private String carDisplayName;
    private String driverTeam;
    private double currentSpeed;  // km/h
    private double currentRpm;
    private int gear;             // 1-5
    private double progress;      // 0.0 (start) to 1.0 (finish)
    private String imageUrl;      // for the car sprite on the map

    public TelemetryDTO() {
    }

    public TelemetryDTO(String carModelId, String driverName, String carDisplayName,
                        String driverTeam, double currentSpeed, double currentRpm,
                        int gear, double progress, String imageUrl) {
        this.carModelId = carModelId;
        this.driverName = driverName;
        this.carDisplayName = carDisplayName;
        this.driverTeam = driverTeam;
        this.currentSpeed = currentSpeed;
        this.currentRpm = currentRpm;
        this.gear = gear;
        this.progress = progress;
        this.imageUrl = imageUrl;
    }

    public String getCarModelId() {
        return carModelId;
    }

    public void setCarModelId(String carModelId) {
        this.carModelId = carModelId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCarDisplayName() {
        return carDisplayName;
    }

    public void setCarDisplayName(String carDisplayName) {
        this.carDisplayName = carDisplayName;
    }

    public String getDriverTeam() {
        return driverTeam;
    }

    public void setDriverTeam(String driverTeam) {
        this.driverTeam = driverTeam;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getCurrentRpm() {
        return currentRpm;
    }

    public void setCurrentRpm(double currentRpm) {
        this.currentRpm = currentRpm;
    }

    public int getGear() {
        return gear;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
