package com.project.back_end;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // To ensure JPA scans entities outside the main package
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // To enable JPA repositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories; // To enable MongoDB repositories

/**
 * The main entry point for the Smart Clinic Management System backend application.
 * This class uses @SpringBootApplication to enable auto-configuration, component scanning,
 * and define the main application context.
 */
@SpringBootApplication // Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
@EntityScan(basePackages = {"com.project.back_end.models"}) // Tells JPA where to find your @Entity classes
@EnableJpaRepositories(basePackages = {"com.project.back_end.repository.mysql"}) // CORRECTED: Specifies base package for JPA repositories (singular 'repository')
@EnableMongoRepositories(basePackages = {"com.project.back_end.repository.mongodb"}) // Specifies base package for MongoDB repositories
public class SmartClinicBackendApplication {

    /**
     * The main method that starts the Spring Boot application.
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartClinicBackendApplication.class, args);
    }

}
