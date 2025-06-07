package com.project.back_end.controller;

import com.project.back_end.models.Appointment;
import com.project.back_end.service.AppointmentService;
import com.project.back_end.service.CentralService; // Import the CentralService
import com.project.back_end.service.TokenService; // Import TokenService to extract userId from token
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling all CRUD operations related to appointments.
 * It provides endpoints for booking, retrieving, updating, and canceling appointments,
 * performing validation on tokens and ensuring proper actions based on user roles.
 */
@RestController // Designates this class as a REST controller.
@RequestMapping("/appointments") // Sets the base URL path for all methods in this controller.
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CentralService centralService; // For token and appointment validation
    private final TokenService tokenService; // For extracting userId from token

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 CentralService centralService,
                                 TokenService tokenService) {
        this.appointmentService = appointmentService;
        this.centralService = centralService;
        this.tokenService = tokenService;
    }

    /**
     * Retrieves a list of appointments for a specific doctor on a given date,
     * with optional filtering by patient name. This endpoint is secured for doctors.
     *
     * @param dateStr The date for which appointments are needed (format YYYY-MM-DD).
     * @param patientName The patient name to filter by (can be "null" or empty string if not filtering).
     * @param token The doctor's authorization token.
     * @return ResponseEntity containing a map with the list of appointments or an error message.
     */
    @GetMapping("/{dateStr}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String dateStr,
            @PathVariable String patientName,
            @PathVariable String token) {

        Long doctorId = tokenService.getUserIdFromToken(token); // Extract doctor ID from token
        if (doctorId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing doctor ID.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, doctorId, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid date format. Please use 'YYYY-MM-DD'.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle "null" string for patientName as an actual null
        String finalPatientName = "null".equalsIgnoreCase(patientName) ? null : patientName;

        Map<String, Object> result = appointmentService.getAppointments(finalPatientName, date, token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Retrieves a list of all upcoming appointments for a specific doctor,
     * with optional filtering by patient name. This endpoint is secured for doctors.
     *
     * @param patientName The patient name to filter by (can be "null" or empty string if not filtering).
     * @param token The doctor's authorization token.
     * @return ResponseEntity containing a map with the list of appointments or an error message.
     */
    @GetMapping("/upcoming/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getUpcomingAppointments(
            @PathVariable String patientName,
            @PathVariable String token) {

        Long doctorId = tokenService.getUserIdFromToken(token); // Extract doctor ID from token
        if (doctorId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or missing doctor ID.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, doctorId, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(new HashMap<>(tokenValidation.getBody()), tokenValidation.getStatusCode());
        }

        // Handle "null" string for patientName as an actual null
        String finalPatientName = "null".equalsIgnoreCase(patientName) ? null : patientName;

        Map<String, Object> result = appointmentService.getUpcomingAppointments(finalPatientName, token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    /**
     * Books a new appointment. This endpoint is secured for patients.
     *
     * @param appointment The Appointment object (containing doctorId, patientId, appointmentTime, etc.) from the request body.
     * @param token The patient's authorization token.
     * @return ResponseEntity with a success or error message and appropriate HTTP status.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        Long patientId = tokenService.getUserIdFromToken(token); // Extract patient ID from token
        if (patientId == null) {
            response.put("message", "Invalid token or missing patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return tokenValidation; // Return the unauthorized response directly
        }

        // Ensure the patient ID in the appointment matches the one from the token
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            response.put("message", "Patient ID in token does not match patient ID in appointment request.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        // Validate appointment time (e.g., if doctor exists and time is available)
        int validationResult = centralService.validateAppointment(appointment);
        if (validationResult == -1) {
            response.put("message", "Doctor not found or invalid doctor ID for appointment.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (validationResult == 0) {
            response.put("message", "Appointment time is unavailable or in the past.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict
        }

        int bookingResult = appointmentService.bookAppointment(appointment);
        if (bookingResult == 1) {
            response.put("message", "Appointment booked successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created
        } else {
            response.put("message", "Failed to book appointment due to an internal error.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    /**
     * Updates an existing appointment. This endpoint is secured for patients.
     *
     * @param appointment The Appointment object with updated details from the request body.
     * @param token The patient's authorization token.
     * @return ResponseEntity with a success or error message and appropriate HTTP status.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        Long patientId = tokenService.getUserIdFromToken(token); // Extract patient ID from token
        if (patientId == null) {
            response.put("message", "Invalid token or missing patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return tokenValidation; // Return the unauthorized response directly
        }

        // Additional check: Ensure the patient modifying the appointment owns it (or is authorized for it)
        // This is crucial for security. The AppointmentService.updateAppointment method does not
        // inherently check ownership based on token, so it should be done here or within the service.
        // For now, we rely on the service to handle updates and assume centralService.validateToken
        // ensures the token is valid for 'patient' role. The service will check if the patient exists.
        // A more robust check might involve fetching the appointment by ID and then comparing its patientId
        // with the patientIdFromToken.
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            response.put("message", "Unauthorized: You can only update your own appointments.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }


        // Delegate to appointmentService for update logic
        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancels an existing appointment. This endpoint is secured for patients.
     *
     * @param id The ID of the appointment to cancel.
     * @param token The patient's authorization token.
     * @return ResponseEntity with a success or error message and appropriate HTTP status.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        Long patientId = tokenService.getUserIdFromToken(token); // Extract patient ID from token
        if (patientId == null) {
            response.put("message", "Invalid token or missing patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Validate token with user ID and expected role
        ResponseEntity<Map<String, String>> tokenValidation = centralService.validateToken(token, patientId, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return tokenValidation; // Return the unauthorized response directly
        }

        // The AppointmentService.cancelAppointment method already has a check to ensure
        // the patient from the token matches the patient who booked the appointment.
        return appointmentService.cancelAppointment(id, token);
    }
}
