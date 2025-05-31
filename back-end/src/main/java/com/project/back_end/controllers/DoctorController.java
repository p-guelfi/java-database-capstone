package com.project.back_end.controller;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Login;
import com.project.back_end.service.DoctorService; // Your DoctorService
import com.project.back_end.service.Service;      // Your central Service for validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController // Designates this as a REST controller
@RequestMapping("${api.path}/doctor") // Sets the base URL path, e.g., /api/v1/doctor
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service; // Injected for token validation and filtering

    // Constructor injection for dependencies
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    /**
     * Handles doctor login requests.
     *
     * @param login The login details (email, password) from the request body.
     * @return ResponseEntity containing a token if successful, or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        // Delegates login validation to the DoctorService
        return doctorService.validateDoctor(login);
    }

    /**
     * Retrieves the available time slots for a specific doctor on a given date.
     * This endpoint is accessible to any authenticated user (doctor, patient, admin).
     *
     * @param user The role of the user making the request (for token validation).
     * @param doctorId The ID of the doctor whose availability is requested.
     * @param date The date for which to fetch availability.
     * @param token The authentication token of the user.
     * @return ResponseEntity with the list of available slots or an error message.
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token) {

        // Validate the token. The 'user' path variable should match the role in the token.
        // We'll extract the actual userId from the token to validate against.
        Long userIdFromToken;
        try {
            userIdFromToken = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify user ID."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, user.toUpperCase(), userIdFromToken);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(
                Map.of("message", "Unauthorized access. " + tokenValidationResponse.getBody().getOrDefault("message", "Invalid token.")),
                tokenValidationResponse.getStatusCode()
            );
        }

        // Fetch availability using DoctorService
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        if (availableSlots.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No available slots found for doctor ID " + doctorId + " on " + date + " or doctor not found."), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(Map.of("message", "Doctor availability retrieved successfully.", "availability", availableSlots), HttpStatus.OK);
        }
    }

    /**
     * Retrieves a list of all doctors.
     * Accessible without explicit token validation for general listing, but you might add it based on requirements.
     *
     * @return ResponseEntity with a list of doctors or an error message.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        if (doctors.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No doctors found."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(Map.of("message", "Doctors retrieved successfully.", "doctors", doctors), HttpStatus.OK);
    }

    /**
     * Adds a new doctor to the system.
     * Requires ADMIN token for authorization.
     *
     * @param doctor The doctor details to be added from the request body.
     * @param token The admin's authentication token.
     * @return ResponseEntity indicating success or failure of the operation.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addNewDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        // Validate token for an ADMIN
        Long adminId;
        try {
            adminId = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify admin."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, "ADMIN", adminId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return new ResponseEntity<>(Map.of("message", "Doctor added to database successfully."), HttpStatus.CREATED); // 201 Created
        } else if (result == -1) {
            return new ResponseEntity<>(Map.of("message", "Doctor already exists with this email."), HttpStatus.CONFLICT); // 409 Conflict
        } else {
            return new ResponseEntity<>(Map.of("message", "Some internal error occurred while adding doctor."), HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    /**
     * Updates an existing doctor's details.
     * Requires ADMIN token for authorization.
     *
     * @param doctor The doctor object with updated details from the request body.
     * @param token The admin's authentication token.
     * @return ResponseEntity indicating success or failure of the update.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        // Validate token for an ADMIN
        Long adminId;
        try {
            adminId = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify admin."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, "ADMIN", adminId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return new ResponseEntity<>(Map.of("message", "Doctor updated successfully."), HttpStatus.OK);
        } else if (result == -1) {
            return new ResponseEntity<>(Map.of("message", "Doctor not found."), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(Map.of("message", "Some internal error occurred while updating doctor."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a doctor by ID.
     * Requires ADMIN token for authorization.
     *
     * @param id The ID of the doctor to be deleted.
     * @param token The admin's authentication token.
     * @return ResponseEntity indicating success or failure of the deletion.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token) {

        // Validate token for an ADMIN
        Long adminId;
        try {
            adminId = service.tokenService.getUserIdFromToken(token);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Invalid token. Could not identify admin."), HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, String>> tokenValidationResponse = service.validateToken(token, "ADMIN", adminId);
        if (tokenValidationResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity<>(tokenValidationResponse.getBody(), tokenValidationResponse.getStatusCode());
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return new ResponseEntity<>(Map.of("message", "Doctor deleted successfully."), HttpStatus.OK);
        } else if (result == -1) {
            return new ResponseEntity<>(Map.of("message", "Doctor not found with ID: " + id), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(Map.of("message", "Some internal error occurred while deleting doctor."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters doctors based on name, time (AM/PM), and specialty.
     * This endpoint is intended for general search and might not require a token for public access,
     * but you can add it if needed. The prompt doesn't specify token validation for this specific filter endpoint.
     *
     * @param name The name of the doctor (partial, optional).
     * @param time The available time for filtering ("AM" or "PM", optional).
     * @param speciality The specialty of the doctor (optional).
     * @return ResponseEntity with a map of filtered doctor data.
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable(required = false) String name,
            @PathVariable(required = false) String time,
            @PathVariable(required = false) String speciality) {

        // Use the central Service's filterDoctor method
        Map<String, Object> filteredDoctors = service.filterDoctor(name, speciality, time);

        if (filteredDoctors.containsKey("doctors") && ((List<?>) filteredDoctors.get("doctors")).isEmpty()) {
            return new ResponseEntity<>(filteredDoctors, HttpStatus.NOT_FOUND); // No doctors found
        }
        return new ResponseEntity<>(filteredDoctors, HttpStatus.OK);
    }
}