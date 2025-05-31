package com.project.back_end.controller;

import com.project.back_end.models.Admin;
import com.project.back_end.service.Service; // Your central Service class
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("${api.path}/admin") // Sets the base URL path for this controller, e.g., /api/v1/admin
public class AdminController {

    private final Service service; // Autowired the central Service class

    // Constructor injection for the Service dependency
    public AdminController(Service service) {
        this.service = service;
    }

    /**
     * Handles admin login requests.
     * Validates admin credentials and returns a JWT token upon successful authentication.
     *
     * @param admin The Admin object containing username and password from the request body.
     * @return ResponseEntity containing a map with a message and token (if successful) or an error message.
     */
    @PostMapping("/login") // Maps POST requests to /api/v1/admin/login
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        // Delegate the login validation to the central Service class
        return service.validateAdmin(admin);
    }
}