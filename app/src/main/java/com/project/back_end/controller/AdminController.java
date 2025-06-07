package com.project.back_end.controller;

import com.project.back_end.models.Admin;
import com.project.back_end.service.CentralService;
import com.project.back_end.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Changed from RestController to Controller
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody; // Import ResponseBody
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.HashMap;

/**
 * Controller for handling Admin-related operations, primarily login and dashboard access.
 * This controller exposes HTTP endpoints for admin authentication and serving the admin dashboard.
 */
@Controller // Changed from @RestController to @Controller
@RequestMapping("/admin")
public class AdminController {

    private final CentralService centralService;
    private final TokenService tokenService;

    @Autowired
    public AdminController(CentralService centralService, TokenService tokenService) {
        this.centralService = centralService;
        this.tokenService = tokenService;
    }

    /**
     * Handles the login request for administrators.
     * It validates the provided admin credentials and, if valid, issues an authentication token.
     *
     * @param admin The Admin object containing username and password from the request body.
     * @return ResponseEntity containing a map with a token on successful login, or an error message on failure.
     */
    @PostMapping("/login")
    @ResponseBody // Explicitly added @ResponseBody as this method returns API data (JSON)
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return centralService.validateAdmin(admin);
    }

    /**
     * Serves the admin dashboard page.
     * This endpoint is accessed after a successful admin login.
     * It validates the token provided in the path to ensure authorized access.
     *
     * @param token The JWT token provided by the authenticated admin.
     * @return The name of the HTML template "adminDashboard" if the token is valid,
     * otherwise redirects to an unauthorized page or returns an error.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Long adminIdFromToken = tokenService.getUserIdFromToken(token);

        ResponseEntity<Map<String, String>> validationResponse = centralService.validateToken(token, adminIdFromToken, "admin");

        if (validationResponse.getStatusCode().equals(HttpStatus.OK)) {
            return "admin/adminDashboard"; // CORRECTED: Now resolves to src/main/resources/templates/admin/adminDashboard.html
        } else {
            System.err.println("Unauthorized access attempt to adminDashboard with token: " + token);
            return "redirect:/error/unauthorized"; // Redirect to a generic unauthorized page
        }
    }
}
