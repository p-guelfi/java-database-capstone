package com.project.back_end.controller;

import com.project.back_end.models.Prescription;
import com.project.back_end.service.PrescriptionService; // Your PrescriptionService
import com.project.back_end.service.Service;          // Your central Service for token validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("${api.path}/prescription") // Sets the base URL path, e.g., /api/v1/prescription
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service; // Injected for token validation

    // Constructor injection for dependencies
    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    /**
     * Saves a new prescription.
     * Only authenticated doctors can save prescriptions.
     *
     * @param token The authentication token for the doctor.
     * @param prescription The prescription details to be saved (from request body).
     * @return ResponseEntity indicating success or failure of the save operation.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        // Extract doctor ID from token for validation
        Long doctorId;
        try {
            doctorId = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify doctor."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token to ensure the request is from a valid doctor
        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, "DOCTOR", doctorId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        // Delegate to PrescriptionService to save the prescription
        Map<String, String> saveResult = prescriptionService.savePrescription(prescription);

        if ("Prescription saved successfully.".equals(saveResult.get("message"))) {
            return new ResponseEntity<>(saveResult, HttpStatus.CREATED); // 201 Created
        } else {
            return new ResponseEntity<>(saveResult, HttpStatus.INTERNAL_SERVER_ERROR); // Or other appropriate status based on PrescriptionService's error types
        }
    }

    /**
     * Retrieves a prescription by its associated appointment ID.
     * Only authenticated doctors can retrieve prescriptions.
     *
     * @param appointmentId The ID of the appointment for which to retrieve the prescription.
     * @param token The authentication token for the doctor.
     * @return ResponseEntity with the prescription details or an error message.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescriptionByAppointmentId(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        // Extract doctor ID from token for validation
        Long doctorId;
        try {
            doctorId = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify doctor."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token to ensure the request is from a valid doctor
        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, "DOCTOR", doctorId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Delegate to PrescriptionService to get the prescription
        Map<String, Object> prescriptionResult = prescriptionService.getPrescription(appointmentId);

        if (prescriptionResult.containsKey("prescription")) {
            return new ResponseEntity<>(prescriptionResult, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(prescriptionResult, HttpStatus.NOT_FOUND); // No prescription found
        }
    }
}