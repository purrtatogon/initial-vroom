package com.initialvroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InitialVroomApplication {

    public static void main(String[] args) {
        SpringApplication.run(InitialVroomApplication.class, args);
    }
}
