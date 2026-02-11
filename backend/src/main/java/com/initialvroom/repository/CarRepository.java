package com.initialvroom.repository;

import com.initialvroom.entity.Car;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the cars collection.
 * Provides CRUD and query methods without writing implementations.
 */
public interface CarRepository extends MongoRepository<Car, String> {
}
