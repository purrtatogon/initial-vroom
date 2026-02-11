package com.initialvroom.service;

import com.initialvroom.dto.TelemetryDTO;
import com.initialvroom.entity.Car;
import com.initialvroom.repository.CarRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Simulates race telemetry and broadcasts it over WebSocket.
 * Uses SimpMessagingTemplate to push data to subscribed clients.
 */
@Service
public class RaceSimulationService {

    private static final String CAR_MODEL_ID = "ae86_takumi_stg1_stock";
    private static final double REDLINE_RPM = 7500.0;
    private static final double MAX_SPEED = 180.0;

    // Hardcoded path of 20 coordinate points (simulating a track segment). Cycles through for first car.
    private static final double[][] PATH = {
            {0.0, 0.0},
            {50.0, 10.0},
            {100.0, 5.0},
            {150.0, 20.0},
            {200.0, 15.0},
            {250.0, 30.0},
            {300.0, 25.0},
            {350.0, 40.0},
            {400.0, 35.0},
            {450.0, 50.0},
            {500.0, 45.0},
            {550.0, 60.0},
            {600.0, 55.0},
            {650.0, 70.0},
            {700.0, 65.0},
            {750.0, 80.0},
            {800.0, 75.0},
            {850.0, 90.0},
            {900.0, 85.0},
            {950.0, 100.0}
    };

    private final SimpMessagingTemplate messagingTemplate;  // Sends messages to WebSocket subscribers
    private final CarRepository carRepository;
    private int pathIndex = 0;  // Cycles through PATH; wraps when it reaches the end

    public RaceSimulationService(SimpMessagingTemplate messagingTemplate, CarRepository carRepository) {
        this.messagingTemplate = messagingTemplate;
        this.carRepository = carRepository;
    }

    @Scheduled(fixedRate = 50)  // Runs every 50ms (~20 broadcasts/sec)
    public void broadcastTelemetry() {
        double[] point = PATH[pathIndex];
        pathIndex = (pathIndex + 1) % PATH.length;

        // Dummy RPM/Speed for demo; replace with real physics in a full simulation
        double dummyRpm = 2000 + (pathIndex * 300) % (int) REDLINE_RPM;
        double dummySpeed = 30 + (pathIndex * 7) % (int) MAX_SPEED;

        // Fetch car metadata (image, driver) from MongoDB for the telemetry payload
        Car car = carRepository.findById(CAR_MODEL_ID).orElse(null);
        String driverName = car != null ? car.getDriverName() : "";
        String carDisplayName = car != null ? car.getCarDisplayName() : "";
        String imageUrl = car != null ? car.getImageUrl() : "";

        TelemetryDTO telemetry = new TelemetryDTO(
                CAR_MODEL_ID,
                driverName,
                carDisplayName,
                dummySpeed,
                dummyRpm,
                point[0],
                point[1],
                imageUrl
        );

        // Broadcast telemetry to all clients subscribed to /topic/race
        messagingTemplate.convertAndSend("/topic/race", telemetry);
    }
}
