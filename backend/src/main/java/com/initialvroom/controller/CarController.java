package com.initialvroom.controller;

import com.initialvroom.entity.Car;
import com.initialvroom.repository.CarRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for fetching car data.
 * Jackson auto-serializes the Car entities to JSON with camelCase keys,
 * which matches the frontend's TypeScript interface field names perfectly.
 */
@RestController
@RequestMapping("/api/cars")
@CrossOrigin(originPatterns = "*")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    // GET /api/cars — returns all 14 cars as JSON
    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // GET /api/cars?stageId=Stage 1 — filters by stage
    // Spring maps the query param automatically thanks to @RequestParam
    @GetMapping(params = "stageId")
    public List<Car> getByStage(@RequestParam String stageId) {
        return carRepository.findByStageId(stageId);
    }
}
