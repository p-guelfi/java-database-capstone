package com.project.back_end.controller;

import com.project_back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService; // Your PatientService
import com.project.back_end.services.AppService;      // Your central Service for validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("${api.path}/patient") // Sets the base URL path, e.g., /api/v1/patient
public class PatientController {

    private final PatientService patientService;
    private final AppService appService; // Injected for token validation and other common functionality

    // Constructor injection for dependencies
    public PatientController(PatientService patientService, AppService appService) {
        this.patientService = patientService;
        this.appService = appService;
    }

    /**
     * Retrieves details for a specific patient.
     * Requires the patient's own token for authorization.
     *
     * @param token The authentication token for the patient.
     * @return ResponseEntity with patient details or an error message.
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        // Extract patient ID from token for validation
        Long patientId;
        try {
            patientId = appService.getTokenService().getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token for the specific patient and role
        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Fetch patient details using PatientService
        // Assuming getPatientDetails might take a String ID if used in path
        // >>>>>>>>>> THE FIX IS ON THE LINE BELOW <<<<<<<<<<
        return patientService.getPatientDetails(String.valueOf(patientId)); // Corrected: Convert Long to String
    }

    /**
     * Creates a new patient record (patient registration/signup).
     * Does not require a token as it's a registration endpoint.
     *
     * @param patient The patient details to be created from the request body.
     * @return ResponseEntity indicating success or failure of the registration.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createNewPatient(@RequestBody Patient patient) {
        // Validate if the patient already exists by email or phone number
        boolean isValidPatient = appService.validatePatient(patient);

        if (!isValidPatient) {
            return new ResponseEntity<>(Map.of("message", "Patient with this email or phone number already exists."), HttpStatus.CONFLICT); // 409 Conflict
        }

        // If validation passes, create the patient
        int result = patientService.createPatient(patient);

        if (result == 1) {
            return new ResponseEntity<>(Map.of("message", "Signup successful."), HttpStatus.CREATED); // 201 Created
        } else {
            return new ResponseEntity<>(Map.of("message", "Internal server error during signup."), HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    /**
     * Handles patient login requests.
     *
     * @param login The login credentials (email, password) from the request body.
     * @return ResponseEntity containing a token if successful, or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        // Delegates login validation to the central Service class
        return appService.validatePatientLogin(login);
    }

    /**
     * Retrieves appointments for a specific patient.
     * Requires the patient's own token for authorization.
     *
     * @param id The ID of the patient (should match ID from token for security).
     * @param token The authentication token for the patient.
     * @return ResponseEntity with the list of patient appointments or an error message.
     */
    @GetMapping("/{id}/{token}") // Note: You might prefer /appointments/{token} and extract ID from token
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token) {

        // Extract patient ID from token for validation
        Long patientIdFromToken;
        try {
            patientIdFromToken = appService.getTokenService().getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        // Validate the token for the specific patient and role
        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientIdFromToken);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Crucial security check: Ensure the ID in the path matches the ID in the token
        if (!id.equals(patientIdFromToken)) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized: Patient ID in path does not match token."), HttpStatus.FORBIDDEN);
        }

        // Fetch patient appointments using PatientService
        // The service.getPatientAppointment method expects Long patientId and String token
        return patientService.getPatientAppointment(id, token);
    }

    /**
     * Filters patient appointments based on condition (e.g., "upcoming", "past") and doctor name.
     * Requires the patient's own token for authorization.
     *
     * @param condition The condition to filter appointments by.
     * @param name The doctor's name or description for filtering.
     * @param token The authentication token for the patient.
     * @return ResponseEntity with the filtered appointments or an error message.
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable(required = false) String name, // Name can be optional
            @PathVariable String token) {

        // Extract patient ID from token for validation (this is handled by service.filterPatient internally)
        // But we still validate the token for the user/role.
        Long patientIdFromToken;
        try {
            patientIdFromToken = appService.getTokenService().getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientIdFromToken);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Delegate filtering to the central Service class
        return appService.filterPatient(condition, name, token);
    }
}