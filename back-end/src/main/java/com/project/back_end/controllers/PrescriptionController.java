package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService; // Your PrescriptionService
import com.project.back_end.services.AppService;          // Your central Service for token validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("${api.path}/prescription") // Sets the base URL path, e.g., /api/v1/prescription
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AppService appService; // Injected for token validation

    // Constructor injection for dependencies
    public PrescriptionController(PrescriptionService prescriptionService, AppService appService) { // CHANGED: parameter type and name
        this.prescriptionService = prescriptionService;
        this.appService = appService; // CHANGED: field assignment
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
            doctorId = appService.getTokenService().getUserIdFromToken(token); // CHANGED: appService.getTokenService()
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify doctor."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token to ensure the request is from a valid doctor
        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "DOCTOR", doctorId); // CHANGED: appService.validateToken()
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        // Delegate to PrescriptionService to save the prescription
        // ASSUMPTION: prescriptionService.savePrescription now returns ResponseEntity<Map<String, String>>
        // If it returns just a Map, you'll need to wrap it.
        return prescriptionService.savePrescription(prescription); // CHANGED: directly returning the ResponseEntity
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
            doctorId = appService.getTokenService().getUserIdFromToken(token); // CHANGED: appService.getTokenService()
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify doctor."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token to ensure the request is from a valid doctor
        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "DOCTOR", doctorId); // CHANGED: appService.validateToken()
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Delegate to PrescriptionService to get the prescription
        // ASSUMPTION: prescriptionService.getPrescription now returns ResponseEntity<Map<String, Object>>
        // If it returns just a Map, you'll need to wrap it.
        return prescriptionService.getPrescription(appointmentId); // CHANGED: directly returning the ResponseEntity
    }
}
