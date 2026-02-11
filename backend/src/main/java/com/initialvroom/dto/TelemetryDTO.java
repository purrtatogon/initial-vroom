package com.initialvroom.dto;

/**
 * Payload sent over WebSocket to /topic/race.
 * Contains live telemetry (position, speed, RPM) and car metadata for the dashboard.
 */
public class TelemetryDTO {

    private String carModelId;
    private String driverName;
    private String carDisplayName;
    private double currentSpeed;
    private double currentRpm;
    private double x;
    private double y;
    private String imageUrl;

    public TelemetryDTO() {
    }

    public TelemetryDTO(String carModelId, String driverName, String carDisplayName,
                        double currentSpeed, double currentRpm, double x, double y, String imageUrl) {
        this.carModelId = carModelId;
        this.driverName = driverName;
        this.carDisplayName = carDisplayName;
        this.currentSpeed = currentSpeed;
        this.currentRpm = currentRpm;
        this.x = x;
        this.y = y;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
