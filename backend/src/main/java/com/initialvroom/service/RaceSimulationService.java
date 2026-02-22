package com.initialvroom.service;

import com.initialvroom.dto.BattleTelemetryDTO;
import com.initialvroom.dto.TelemetryDTO;
import com.initialvroom.entity.Car;
import com.initialvroom.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * The heart of the project: simulates a two-car race in real time.
 * Runs on a 50ms loop (20 ticks/sec), computes speed/position for each car,
 * and pushes telemetry over WebSocket so the dashboard stays live.
 *
 * Not a physics engine — it's a simplified model based on power-to-weight ratio
 * that produces plausible telemetry data for a dashboard project.
 */
@Service
public class RaceSimulationService {

    private static final Logger log = LoggerFactory.getLogger(RaceSimulationService.class);
    private static final double TRACK_LENGTH_M = 5000.0;  // race is 5km
    private static final double TICK_SECONDS = 0.05;       // 50ms per tick = 20 fps

    // SimpMessagingTemplate is Spring's way to push messages to WebSocket topics
    private final SimpMessagingTemplate messagingTemplate;
    private final CarRepository carRepository;
    private final Random random = new Random();

    // Race state — volatile because the scheduler thread and HTTP threads both access it
    private volatile boolean battleActive = false;
    private Car car1;
    private Car car2;
    private double car1DistanceM;
    private double car2DistanceM;
    private double car1SpeedKmh;
    private double car2SpeedKmh;
    private long startTimeMs;

    public RaceSimulationService(SimpMessagingTemplate messagingTemplate, CarRepository carRepository) {
        this.messagingTemplate = messagingTemplate;
        this.carRepository = carRepository;
    }

    /**
     * Called by BattleController when the user clicks "Start Battle".
     * Looks up both cars in MongoDB and resets all race state.
     * Synchronized to prevent two battles starting at the same time.
     */
    public synchronized boolean startBattle(String car1Id, String car2Id) {
        Car c1 = carRepository.findById(car1Id).orElse(null);
        Car c2 = carRepository.findById(car2Id).orElse(null);
        if (c1 == null || c2 == null) {
            log.warn("Battle start failed: car1={} car2={}", car1Id, car2Id);
            return false;
        }

        this.car1 = c1;
        this.car2 = c2;
        this.car1DistanceM = 0;
        this.car2DistanceM = 0;
        this.car1SpeedKmh = 0;
        this.car2SpeedKmh = 0;
        this.startTimeMs = System.currentTimeMillis();
        this.battleActive = true;

        log.info("Battle started: {} vs {}", c1.getCarDisplayName(), c2.getCarDisplayName());
        return true;
    }

    public synchronized void stopBattle() {
        this.battleActive = false;
        log.info("Battle stopped");
    }

    /**
     * The main simulation loop. Runs every 50ms thanks to @Scheduled.
     * Each tick: compute new speeds → update distances → check finish → broadcast telemetry.
     */
    @Scheduled(fixedRate = 50)
    public void tick() {
        if (!battleActive) {
            return;
        }

        boolean finished = false;

        // Compute new speed for each car based on their stats and track position
        car1SpeedKmh = computeSpeed(car1, car1DistanceM, car1SpeedKmh);
        car2SpeedKmh = computeSpeed(car2, car2DistanceM, car2SpeedKmh);

        // Convert km/h to m/s (÷3.6), then multiply by tick duration to get distance traveled
        car1DistanceM += (car1SpeedKmh / 3.6) * TICK_SECONDS;
        car2DistanceM += (car2SpeedKmh / 3.6) * TICK_SECONDS;

        // Check if either car crossed the finish line
        if (car1DistanceM >= TRACK_LENGTH_M || car2DistanceM >= TRACK_LENGTH_M) {
            // Clamp so progress never exceeds 1.0 on the frontend SVG
            car1DistanceM = Math.min(car1DistanceM, TRACK_LENGTH_M);
            car2DistanceM = Math.min(car2DistanceM, TRACK_LENGTH_M);
            finished = true;
        }

        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        double gapMeters = Math.abs(car1DistanceM - car2DistanceM);

        // Build telemetry snapshots and broadcast to all WebSocket subscribers
        TelemetryDTO t1 = buildTelemetry(car1, car1DistanceM, car1SpeedKmh);
        TelemetryDTO t2 = buildTelemetry(car2, car2DistanceM, car2SpeedKmh);

        BattleTelemetryDTO battle = new BattleTelemetryDTO(t1, t2, gapMeters, elapsedMs, finished);
        messagingTemplate.convertAndSend("/topic/race", battle);

        if (finished) {
            battleActive = false;
            // Winner is determined by distance; the frontend also figures this out from progress
            String winner = car1DistanceM >= car2DistanceM
                    ? car1.getDriverName() : car2.getDriverName();
            log.info("Battle finished! Winner: {} (gap: {}m)", winner, String.format("%.1f", gapMeters));
        }
    }

    /**
     * Core speed calculation for each car per tick.
     * Uses power-to-weight ratio as the single driving metric — this is the most
     * important performance number in real motorsport too.
     */
    private double computeSpeed(Car car, double distanceM, double currentSpeedKmh) {
        // Power-to-weight ratio: higher = faster car
        double pwRatio = (double) car.getCarHp() / car.getWeightKg();
        double maxSpeed = 80.0 + pwRatio * 300.0;         // top speed on straights
        double acceleration = pwRatio * 120.0;              // how quickly it gets there

        double progressFraction = distanceM / TRACK_LENGTH_M;
        boolean inHairpin = isInHairpin(progressFraction);

        double targetSpeed;
        if (inHairpin) {
            // Slow down through hairpins — target speed is much lower
            targetSpeed = 40.0 + pwRatio * 60.0;
            // 4WD gets better grip through corners (8% bonus)
            if ("4WD".equals(car.getDrivetrainCode())) {
                targetSpeed *= 1.08;
            }
        } else {
            targetSpeed = maxSpeed;
            // Turbo cars get a small boost on straights (4% bonus)
            if ("Turbo".equals(car.getAspirationType()) || "Twin Turbo".equals(car.getAspirationType())) {
                targetSpeed *= 1.04;
            }
        }

        // Random jitter (±2 km/h) so the same matchup doesn't play out identically every time
        double jitter = (random.nextDouble() - 0.5) * 4.0;
        targetSpeed += jitter;

        // Accelerate toward target speed, or brake if we're going too fast
        double newSpeed;
        if (currentSpeedKmh < targetSpeed) {
            newSpeed = currentSpeedKmh + acceleration * TICK_SECONDS;
            newSpeed = Math.min(newSpeed, targetSpeed);
        } else {
            // Braking is 2.5x harder than acceleration — simulates hard braking into hairpins
            double braking = acceleration * 2.5;
            newSpeed = currentSpeedKmh - braking * TICK_SECONDS;
            newSpeed = Math.max(newSpeed, targetSpeed);
        }

        return Math.max(0, newSpeed);
    }

    /**
     * Five hairpin zones spread across the track, inspired by Akina's consecutive hairpins.
     * Each zone is a range of track progress (0.0–1.0) where cars must slow down.
     * These don't correspond to the SVG curves — they're separate abstractions.
     */
    private boolean isInHairpin(double progress) {
        double[][] hairpins = {
                {0.15, 0.20},
                {0.30, 0.35},
                {0.45, 0.50},
                {0.60, 0.65},
                {0.78, 0.83}
        };
        for (double[] h : hairpins) {
            if (progress >= h[0] && progress <= h[1]) {
                return true;
            }
        }
        return false;
    }

    /** Packages a car's current state into a DTO for the WebSocket payload. */
    private TelemetryDTO buildTelemetry(Car car, double distanceM, double speedKmh) {
        double progress = Math.min(distanceM / TRACK_LENGTH_M, 1.0);
        int gear = computeGear(speedKmh, car.getRedlineRpm());
        double rpm = computeRpm(speedKmh, gear, car.getRedlineRpm());

        return new TelemetryDTO(
                car.getCarModelId(),
                car.getDriverName(),
                car.getCarDisplayName(),
                car.getDriverTeam(),
                Math.round(speedKmh * 10.0) / 10.0,  // round to 1 decimal
                Math.round(rpm),
                gear,
                progress,
                car.getImageUrl()
        );
    }

    /**
     * Gear is just a step function of speed — purely cosmetic for the dashboard display.
     * Not modeling actual gear ratios or shift points; that would be a whole project on its own.
     */
    private int computeGear(double speedKmh, int redlineRpm) {
        if (speedKmh < 30) return 1;
        if (speedKmh < 60) return 2;
        if (speedKmh < 100) return 3;
        if (speedKmh < 140) return 4;
        return 5;
    }

    /**
     * RPM interpolates linearly within each gear's speed band, from idle (1000) to redline.
     * Cars with higher redline (like the S2000 at 9000) show a wider RPM sweep on the gauge.
     * Again, cosmetic — RPM doesn't feed back into the speed calculation.
     */
    private double computeRpm(double speedKmh, int gear, int redlineRpm) {
        double rpmFraction;
        switch (gear) {
            case 1: rpmFraction = speedKmh / 30.0; break;
            case 2: rpmFraction = (speedKmh - 30.0) / 30.0; break;
            case 3: rpmFraction = (speedKmh - 60.0) / 40.0; break;
            case 4: rpmFraction = (speedKmh - 100.0) / 40.0; break;
            default: rpmFraction = (speedKmh - 140.0) / 60.0; break;
        }
        rpmFraction = Math.max(0, Math.min(1, rpmFraction));
        double idleRpm = 1000;
        return idleRpm + rpmFraction * (redlineRpm - idleRpm);
    }
}
