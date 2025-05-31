package com.project.back_end.controller; // THIS IS YOUR CORRECT BASE PACKAGE + .controller

import com.project.back_end.service.AuthService; // THIS IS YOUR CORRECT BASE PACKAGE + .service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    // Autowire the AuthService. We'll assume this service exists or will be created soon.
    private final AuthService authService;

    @Autowired
    public DashboardController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles requests to the admin dashboard, validating the provided token.
     * @param token The authentication token from the path variable.
     * @return The Thymeleaf view name for the admin dashboard or a redirect to login.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, String> validationResult = authService.validateToken(token, "admin");

        if (validationResult.isEmpty()) { // If the map is empty, token is valid
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
        Map<String, String> validationResult = authService.validateToken(token, "doctor");

        if (validationResult.isEmpty()) { // If the map is empty, token is valid
            // Thymeleaf will resolve this to src/main/resources/templates/doctor/doctorDashboard.html
            return "doctor/doctorDashboard";
        } else {
            // If token is invalid or role mismatch, redirect to the login page
            return "redirect:/"; // Redirects to http://localhost:8080/
        }
    }
}
