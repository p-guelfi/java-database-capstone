package com.project.back_end.controller;

import com.project.back_end.dto.LoginDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.service.CentralService;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling operations related to the Patient entity.
 * It provides endpoints for patient registration, login, fetching patient details,
 * and getting/filtering patient appointments.
 */
@RestController // Designates this class as a REST controller.
@RequestMapping("/patient") // Sets the base URL path for all methods in this controller to /patient.
public class PatientController {

    private final PatientService patientService;
    private final CentralService centralService; // For token validation and general services
    private final TokenService tokenService; // For extracting userId from token

    @Autowired
    public PatientController(PatientService patientService,
                             CentralService centralService,
                             TokenService tokenService) {
        this.patientService = patientService;
        this.centralService = centralService;
        this.tokenService = tokenService;
    }

    /**
     * Fetches the patient's details based on the provided JWT token.
     *
     * @param token The authentication token for the patient.
     * @return ResponseEntity containing the patient's details or an error message.
     */
    @GetMapping("/{token}") // Maps GET requests to /patient/{token}
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        // Extract patient ID from token for validation
        Long patientId = tokenService.getUserIdFromToken(token);
        if (patientId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing patient ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        // Delegate to patientService to get details
        return patientService.getPatientDetails(token);
    }

    /**
     * Creates a new patient record in the system.
     * This endpoint handles patient registration.
     *
     * @param patient The Patient object (name, email, password, phone, address) from the request body.
     * @return ResponseEntity with a success or error message and appropriate HTTP status.
     */
    @PostMapping // Maps POST requests to /patient
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();

        // Validate if the patient already exists by email or phone
        if (!centralService.validatePatient(patient)) { // validatePatient returns true if patient DOES NOT exist
            response.put("message", "Patient with email or phone number already exists.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict
        }

        int result = patientService.createPatient(patient);
        if (result == 1) {
            response.put("message", "Signup successful!");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created
        } else {
            response.put("message", "Internal server error during patient creation.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    /**
     * Handles patient login functionality.
     *
     * @param login The login credentials (email, password) from the request body.
     * @return ResponseEntity containing a token on successful login, or an error message.
     */
    @PostMapping("/login") // Maps POST requests to /patient/login
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody LoginDTO login) {
        // Delegate to centralService for patient login validation and token generation
        return centralService.validatePatientLogin(login);
    }

    /**
     * Retrieves a list of appointments for a specific patient.
     *
     * @param id The ID of the patient.
     * @param token The authentication token for the patient.
     * @return ResponseEntity containing the list of patient appointments or an error message.
     */
    @GetMapping("/{id}/{token}") // Maps GET requests to /patient/{id}/{token}
    public ResponseEntity<Map<String, Object>> getPatientAppointments(@PathVariable Long id, @PathVariable String token) {
        // Extract patient ID from token for validation
        Long patientId = tokenService.getUserIdFromToken(token);
        if (patientId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing patient ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        // Ensure the ID in the path matches the ID from the token for security
        if (!patientId.equals(id)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Forbidden: You can only view your own appointments.");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Delegate to patientService to get appointments
        return patientService.getPatientAppointment(id, token);
    }

    /**
     * Filters patient appointments based on various criteria.
     *
     * @param condition The condition to filter appointments (e.g., "past", "future"). Defaults to "null".
     * @param name The doctor's name to filter by (can be partial). Defaults to "null".
     * @param token The authentication token for the patient.
     * @return ResponseEntity containing the filtered list of patient appointments or an error message.
     */
    @GetMapping("/filter/{condition}/{name}/{token}") // Maps GET requests to /patient/filter/{condition}/{name}/{token}
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        // Extract patient ID from token for validation
        Long patientId = tokenService.getUserIdFromToken(token);
        if (patientId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing patient ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        // Handle "null" strings from path variables as actual null for optional parameters
        String finalCondition = "null".equalsIgnoreCase(condition) ? null : condition;
        String finalName = "null".equalsIgnoreCase(name) ? null : name;

        // Delegate to centralService for filtering patient appointments
        return centralService.filterPatient(finalCondition, finalName, token);
    }
}
