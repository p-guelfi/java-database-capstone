package com.project.back_end.controller;

import com.project.back_end.models.Prescription;
import com.project.back_end.service.CentralService;
import com.project.back_end.service.PrescriptionService;
import com.project.back_end.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling operations related to prescriptions in the system.
 * It provides endpoints for doctors to save and retrieve prescriptions based on appointment ID.
 */
@RestController // Designates this class as a REST controller.
@RequestMapping("/prescription") // Sets the base URL path for all methods in this controller to /prescription.
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final CentralService centralService; // For token validation
    private final TokenService tokenService; // For extracting userId from token

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService,
                                  CentralService centralService,
                                  TokenService tokenService) {
        this.prescriptionService = prescriptionService;
        this.centralService = centralService;
        this.tokenService = tokenService;
    }

    /**
     * Saves a new prescription to the database. This endpoint is secured for doctors.
     *
     * @param token The authentication token for the doctor.
     * @param prescription The prescription details to be saved, from the request body.
     * @return ResponseEntity with a message indicating the result of the save operation.
     * Returns 201 Created on success, or an error message with appropriate HTTP status.
     */
    @PostMapping("/{token}") // Maps POST requests to /prescription/{token}
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token, @RequestBody Prescription prescription) {
        // Extract doctor ID from token for validation
        Long doctorId = tokenService.getUserIdFromToken(token);
        if (doctorId == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing doctor ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role for doctor
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, doctorId, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return tokenValidation; // Return the unauthorized response directly
        }

        // Delegate to prescriptionService to save the prescription
        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Retrieves the prescription(s) associated with a specific appointment ID.
     * This endpoint is secured for doctors.
     *
     * @param appointmentId The ID of the appointment to retrieve the prescription for.
     * @param token The authentication token for the doctor.
     * @return ResponseEntity containing the prescription details or an error message.
     * Returns 200 OK with data, 404 Not Found if no prescriptions, or 500 Internal Server Error on failure.
     */
    @GetMapping("/{appointmentId}/{token}") // Maps GET requests to /prescription/{appointmentId}/{token}
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        // Extract doctor ID from token for validation
        Long doctorId = tokenService.getUserIdFromToken(token);
        if (doctorId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing doctor ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role for doctor
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, doctorId, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        // Delegate to prescriptionService to get the prescription
        return prescriptionService.getPrescription(appointmentId);
    }
}
