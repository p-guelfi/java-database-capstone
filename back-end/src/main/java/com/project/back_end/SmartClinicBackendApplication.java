package com.project.back_end;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
// These annotations tell Spring Data where to find your repository interfaces.
// Assuming your MySQL repositories will be in a sub-package like 'repositories.mysql'
@EnableJpaRepositories("com.project.back_end.repositories.mysql")
// Assuming your MongoDB repositories will be in a sub-package like 'repositories.mongodb'
@EnableMongoRepositories("com.project.back_end.repositories.mongodb")
public class SmartClinicBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartClinicBackendApplication.class, args);
    }

}