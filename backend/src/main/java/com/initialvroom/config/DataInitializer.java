package com.initialvroom.config;

import com.opencsv.bean.CsvToBeanBuilder;
import com.initialvroom.entity.Car;
import com.initialvroom.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads car data from CSV files into MongoDB when the app starts up.
 * CommandLineRunner is a Spring interface — its run() fires right after the context is ready.
 * This way we don't need a manual migration script or a separate data loader tool.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // These paths are relative to the classpath (src/main/resources/)
    private static final String STAGE1_CSV = "data/stage1_battle cars.csv";
    private static final String STAGE2_CSV = "data/stage2_battle cars.csv";

    private final CarRepository carRepository;

    // Spring injects CarRepository automatically (constructor injection)
    public DataInitializer(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if the collection is empty — prevents duplicates on every restart
        if (carRepository.count() > 0) {
            log.info("Cars collection already populated, skipping data initialization.");
            return;
        }

        log.info("Cars collection is empty, loading data from CSV files...");

        List<Car> allCars = new java.util.ArrayList<>();

        loadCsv(allCars, STAGE1_CSV);
        loadCsv(allCars, STAGE2_CSV);

        // saveAll does a single batch insert — much faster than calling save() 14 times
        carRepository.saveAll(allCars);
        log.info("Loaded {} cars into MongoDB.", allCars.size());
    }

    private void loadCsv(List<Car> allCars, String resourceName) {
        try (Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resourceName),
                StandardCharsets.UTF_8)) {

            // CsvToBeanBuilder maps each CSV row to a Car object using @CsvBindByName annotations
            List<Car> cars = new CsvToBeanBuilder<Car>(reader)
                    .withType(Car.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            allCars.addAll(cars);
            log.info("Loaded {} cars from {}", cars.size(), resourceName);

        } catch (Exception e) {
            log.error("Failed to load CSV: {}", resourceName, e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }
}
