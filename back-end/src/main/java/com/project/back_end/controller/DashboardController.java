package com.project.back_end.controller;

import com.project.back_end.services.AppService; // Corrected import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping // You might want to add a base request mapping here if all dashboard endpoints start with something common, e.g., "/dashboard"
public class DashboardController {

    private final AppService appService; // Field is correctly named appService

    @Autowired
    public DashboardController(AppService appService) { // Constructor parameter and assignment corrected
        this.appService = appService;
    }

    /**
     * Handles requests to the admin dashboard, validating the provided token.
     * @param token The authentication token from the path variable.
     * @return The Thymeleaf view name for the admin dashboard or a redirect to login.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // Your AppService.validateToken expects: String token, String userRole, Long userId
        // Here, we have the token and the role "ADMIN".
        // You'll need to determine how to get the userId for validation.
        // For a simple view controller, you might validate just role, or extract userId from the token itself.
        // For now, userId is passed as null, which might need adjustment based on your TokenService.validateToken implementation.
        ResponseEntity<Map<String, String>> validationResponse = appService.validateToken(token, "ADMIN", null); // Usage corrected to appService

        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // Thymeleaf will resolve this to src/main/resources/templates/admin/adminDashboard.html
            return "admin/adminDashboard";
        } else {
            // If token is invalid or role mismatch, redirect to the login page
            return "redirect:/"; // Redirects to http://localhost:8080/
        }
    }

    /**
     * Handles requests to the doctor dashboard, validating the provided token.
     * @param token The authentication token from the path variable.
     * @return The Thymeleaf view name for the doctor dashboard or a redirect to login.
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        // Similar to adminDashboard, adjust userId extraction as necessary
        ResponseEntity<Map<String, String>> validationResponse = appService.validateToken(token, "DOCTOR", null); // Usage corrected to appService

        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // Thymeleaf will resolve this to src/main/resources/templates/doctor/doctorDashboard.html
            return "doctor/doctorDashboard";
        } else {
            // If token is invalid or role mismatch, redirect to the login page
            return "redirect:/"; // Redirects to http://localhost:8080/
        }
    }
}