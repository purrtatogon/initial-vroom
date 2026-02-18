package com.initialvroom.repository;

import com.initialvroom.entity.Car;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data auto-implements this interface — no code needed for findAll, findById, save, etc.
 * The generic <Car, String> means: Car entity, String type for the @Id field.
 */
public interface CarRepository extends MongoRepository<Car, String> {

    // Spring generates the Mongo query from the method name: { stageId: ? }
    // Used by GET /api/cars?stageId=Stage 1
    List<Car> findByStageId(String stageId);
}
