package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient; // ADDED THIS IMPORT
import com.project.back_end.services.AppointmentService; // Your AppointmentService
import com.project.back_end.services.AppService;          // Your central Service for validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("/appointments") // Sets the base URL path for this controller
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppService appService; // Renamed to appService

    // Constructor injection for dependencies
    public AppointmentController(AppointmentService appointmentService, AppService appService) { // RENAMED PARAMETER TYPE AND NAME
        this.appointmentService = appointmentService;
        this.appService = appService; // RENAMED FIELD ASSIGNMENT
    }

    /**
     * Retrieves appointments based on date and optionally patient name.
     * This endpoint is intended for doctors to view appointments.
     *
     * @param date The date for which to retrieve appointments (format YYYY-MM-DD).
     * @param patientName The name of the patient to filter by (optional).
     * @param token The doctor's authentication token.
     * @return A ResponseEntity containing a map with appointment details or an error message.
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable(required = false) String patientName, // patientName is optional
            @PathVariable String token) {

        // Validate token for a doctor
        // Note: The 'appService.validateToken' assumes it needs userId and userRole.
        // For this endpoint, we'd typically get the doctor's ID from the token first.
        // Assuming appService.validateToken is flexible enough, or we need to adjust its usage here.
        // For now, I'll pass a placeholder userId (0L) and rely on the role check primarily.
        // In a real app, you'd extract doctor ID from the token via appService.getTokenService().getUserIdFromToken(token)
        // and then pass that actual doctor ID to validateToken.
        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "DOCTOR", 0L); // CHANGED 'service' to 'appService'
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Delegate to AppointmentService to fetch appointments
        Map<String, Object> appointments = appointmentService.getAppointment(patientName, date, token);

        if (appointments.containsKey("message") && appointments.get("message").toString().startsWith("No appointments")) {
            return new ResponseEntity<>(appointments, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Books a new appointment.
     * Only authenticated patients can book appointments.
     *
     * @param appointment The Appointment object to be booked (from request body).
     * @param token The patient's authentication token.
     * @return ResponseEntity indicating success (201 Created) or failure.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // Validate token for a patient
        // Similar to getAppointments, extract patient ID from token before validating
        Long patientId = null;
        try {
            patientId = appService.getTokenService().getUserIdFromToken(token); // CHANGED 'service.tokenService' to 'appService.getTokenService()'
        } catch (Exception e) {
             return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientId); // CHANGED 'service' to 'appService'
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        // Ensure the appointment's patient ID matches the authenticated token's patient ID
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized: Appointment patient ID mismatch with token."), HttpStatus.FORBIDDEN);
        }

        // Validate appointment details (e.g., doctor availability, overlaps) using central Service
        int validationResult = appService.validateAppointment(appointment); // CHANGED 'service' to 'appService'

        if (validationResult == -1) {
            return new ResponseEntity<>(Map.of("message", "Doctor does not exist."), HttpStatus.BAD_REQUEST);
        } else if (validationResult == 0) {
            return new ResponseEntity<>(Map.of("message", "Appointment time is unavailable or overlaps with existing appointments."), HttpStatus.CONFLICT);
        } else if (validationResult == -2) {
            return new ResponseEntity<>(Map.of("message", "Invalid time format in appointment request."), HttpStatus.BAD_REQUEST);
        }

        // If validation passes, proceed to book the appointment
        int bookingResult = appointmentService.bookAppointment(appointment);

        if (bookingResult == 1) {
            return new ResponseEntity<>(Map.of("message", "Appointment booked successfully."), HttpStatus.CREATED); // 201 Created
        } else {
            return new ResponseEntity<>(Map.of("message", "Failed to book appointment due to an internal error."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing appointment.
     * Only authenticated patients can update their appointments.
     *
     * @param token The patient's authentication token.
     * @param appointment The Appointment object with updated details (from request body).
     * @return ResponseEntity indicating success or failure of the update.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        Long patientId = null;
        try {
            patientId = appService.getTokenService().getUserIdFromToken(token); // CHANGED 'service.tokenService' to 'appService.getTokenService()'
        } catch (Exception e) {
             return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientId); // CHANGED 'service' to 'appService'
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        // Ensure the appointment being updated belongs to the authenticated patient
        // This requires fetching the existing appointment first to check its patient ID
        // For simplicity, we'll rely on the AppointmentService to handle this internally if not passed.
        // Ideally, you'd fetch the existing appointment here to confirm ownership before proceeding.
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
             // If the patient object in the request is not set or doesn't match the token's ID, set it for the service
            appointment.setPatient(new Patient()); // Create a dummy patient to hold the ID
            appointment.getPatient().setId(patientId); // Set the ID from the token
        }


        // Delegate to AppointmentService to update
        ResponseEntity<Map<String, String>> updateResponse = appointmentService.updateAppointment(appointment);
        return updateResponse;
    }

    /**
     * Cancels an existing appointment.
     * Only authenticated patients can cancel their appointments.
     *
     * @param id The ID of the appointment to cancel.
     * @param token The patient's authentication token.
     * @return ResponseEntity indicating success or failure of the cancellation.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        // Validate token for a patient
        // The cancelAppointment in AppointmentService already validates the patient ID against the token.
        // So, we just need to ensure the token itself is valid for a patient role.
        Long patientId = null;
        try {
            patientId = appService.getTokenService().getUserIdFromToken(token); // CHANGED 'service.tokenService' to 'appService.getTokenService()'
        } catch (Exception e) {
             return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify patient."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = appService.validateToken(token, "PATIENT", patientId); // CHANGED 'service' to 'appService'
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        // Delegate to AppointmentService to cancel
        ResponseEntity<Map<String, String>> cancelResponse = appointmentService.cancelAppointment(id, token);
        return cancelResponse;
    }
}
