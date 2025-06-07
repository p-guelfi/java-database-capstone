package com.project.back_end.controller;

import com.project.back_end.dto.LoginDTO; // New import for LoginDTO
import com.project.back_end.models.Doctor;
import com.project.back_end.models.DoctorAvailableTime;
import com.project.back_end.service.CentralService;
import com.project.back_end.service.DoctorService;
import com.project.back_end.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for handling operations related to Doctor entities.
 * Includes endpoints for managing doctor profiles, recurring availability, and now, doctor login.
 */
@RestController // Designates this class as a REST controller for handling HTTP requests.
@RequestMapping("/doctor") // Base URL path for all methods in this controller
public class DoctorController {

    private final DoctorService doctorService;
    private final TokenService tokenService;
    private final CentralService centralService; // For token validation and login validation

    @Autowired
    public DoctorController(DoctorService doctorService, TokenService tokenService, CentralService centralService) {
        this.doctorService = doctorService;
        this.tokenService = tokenService;
        this.centralService = centralService;
    }

    /**
     * Handles login requests for doctors.
     * Delegates validation to CentralService and returns a JWT token on success.
     * This endpoint is crucial for the doctor login flow.
     * @param loginDTO The Doctor's login credentials (email and password).
     * @return ResponseEntity with a JWT token and success message, or an error message.
     */
    @PostMapping("/login") // This endpoint handles POST requests to /doctor/login
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody LoginDTO loginDTO) {
        return centralService.validateDoctorLogin(loginDTO);
    }

    /**
     * Retrieves a list of all doctors. (Public endpoint, potentially for patient browsing)
     * @return ResponseEntity containing a list of doctors or an error message.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDoctors() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        response.put("doctors", doctors);
        response.put("message", "Doctors retrieved successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a single doctor's profile by ID. Secured for the doctor themselves.
     * @param id The ID of the doctor to retrieve.
     * @param token The doctor's authentication token.
     * @return ResponseEntity containing the doctor's profile or an error.
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorById(@PathVariable Long id, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        Long doctorIdFromToken = tokenService.getUserIdFromToken(token);
        // Validate token belongs to this doctor and has 'doctor' role
        if (doctorIdFromToken == null || !doctorIdFromToken.equals(id) || !centralService.validateToken(token, doctorIdFromToken, "doctor").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized access or invalid token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Optional<Doctor> doctorOpt = doctorService.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            // Create a map to return only non-sensitive data
            Map<String, Object> doctorMap = new HashMap<>();
            doctorMap.put("id", doctor.getId());
            doctorMap.put("name", doctor.getName());
            doctorMap.put("email", doctor.getEmail());
            doctorMap.put("phone", doctor.getPhone());
            doctorMap.put("specialty", doctor.getSpecialty());
            // Do NOT include password or other sensitive fields in the response to frontend

            response.put("doctor", doctorMap);
            response.put("message", "Doctor profile retrieved successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Doctor not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Adds a new doctor to the system. (Admin-only)
     * Changed path to /admin-add/{token} to prevent conflicts with /doctor/login.
     * @param doctor The Doctor object to add.
     * @param token The admin's authorization token.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/admin-add/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        Long adminId = tokenService.getUserIdFromToken(token); // Assuming tokenService can get admin ID
        if (adminId == null || !centralService.validateToken(token, adminId, "admin").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized to add doctor. Admin token required.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Basic validation for doctor fields
        if (doctor.getEmail() == null || doctor.getEmail().isEmpty()) {
            response.put("message", "Doctor email cannot be empty.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            doctorService.saveDoctor(doctor);
            response.put("message", "Doctor added successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error adding doctor: " + e.getMessage());
            response.put("message", "Failed to add doctor: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing doctor's profile. Secured for the doctor themselves.
     * @param id The ID of the doctor to update (from path).
     * @param updatedDoctor The Doctor object with updated fields (from request body).
     * @param token The doctor's authorization token.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDoctor, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        Long doctorIdFromToken = tokenService.getUserIdFromToken(token);
        // Validate token belongs to this doctor and has 'doctor' role
        if (doctorIdFromToken == null || !doctorIdFromToken.equals(id) || !centralService.validateToken(token, doctorIdFromToken, "doctor").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized to update this profile.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Ensure the ID in the path matches the ID in the request body for consistency
        if (!id.equals(updatedDoctor.getId())) {
            response.put("message", "ID in path does not match Doctor ID in request body.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int result = doctorService.updateDoctor(updatedDoctor); // Call service to update
        if (result == 1) {
            response.put("message", "Doctor profile updated successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Doctor not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Failed to update doctor profile due to an internal error.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a doctor by ID. (Admin-only)
     * @param id The ID of the doctor to delete.
     * @param token The admin's authorization token.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        Long adminId = tokenService.getUserIdFromToken(token); // Assuming tokenService can get admin ID
        if (adminId == null || !centralService.validateToken(token, adminId, "admin").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized to delete doctor. Admin token required.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            doctorService.deleteDoctor(id);
            response.put("message", "Doctor deleted successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            response.put("message", "Failed to delete doctor: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters doctors based on name, time availability, and specialty. (Public view)
     * @param name The doctor's name to filter by. Defaults to "null".
     * @param time The available time slot to filter by. Defaults to "null".
     * @param specialty The doctor's specialty to filter by. Defaults to "null".
     * @return ResponseEntity containing a map with the filtered list of doctors.
     */
    @GetMapping("/filter/{name}/{time}/{specialty}") // NEW: Endpoint for filtering doctors
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String specialty) {
        Map<String, Object> response = new HashMap<>();

        // Ensure "null" string from path variables are converted to actual null for service layer
        String actualName = "null".equalsIgnoreCase(name) ? null : name;
        String actualTime = "null".equalsIgnoreCase(time) ? null : time;
        String actualSpecialty = "null".equalsIgnoreCase(specialty) ? null : specialty;

        List<Doctor> doctors = doctorService.filterDoctors(actualName, actualTime, actualSpecialty);
        response.put("doctors", doctors);
        response.put("message", "Doctors filtered successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // --- NEW ENDPOINTS for Doctor Available Times ---

    /**
     * Retrieves all recurring available time slots for a specific doctor.
     * Secured for the doctor themselves.
     * @param id The ID of the doctor whose available times to retrieve.
     * @param token The doctor's authentication token.
     * @return ResponseEntity containing a list of available times.
     */
    @GetMapping("/{id}/available-times/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailableTimes(
            @PathVariable Long id, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        Long doctorIdFromToken = tokenService.getUserIdFromToken(token);
        if (doctorIdFromToken == null || !doctorIdFromToken.equals(id) || !centralService.validateToken(token, doctorIdFromToken, "doctor").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized access or invalid token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<DoctorAvailableTime> availableTimes = doctorService.getDoctorAvailableTimes(id);
        response.put("availableTimes", availableTimes);
        response.put("message", "Doctor available times retrieved successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Adds a new recurring available time slot for a doctor.
     * Secured for the doctor themselves.
     * @param id The ID of the doctor.
     * @param token The doctor's authentication token.
     * @param requestBody Map containing "availableTime" string (e.g., "09:00-10:00").
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/{id}/available-times/{token}")
    public ResponseEntity<Map<String, String>> addDoctorAvailableTime(
            @PathVariable Long id, @PathVariable String token, @RequestBody Map<String, String> requestBody) {
        Map<String, String> response = new HashMap<>();

        Long doctorIdFromToken = tokenService.getUserIdFromToken(token);
        if (doctorIdFromToken == null || !doctorIdFromToken.equals(id) || !centralService.validateToken(token, doctorIdFromToken, "doctor").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized to add available time.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String availableTime = requestBody.get("availableTime");
        if (availableTime == null || availableTime.trim().isEmpty()) {
            response.put("message", "Available time string cannot be empty.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int result = doctorService.addDoctorAvailableTime(id, availableTime);
        if (result == 1) {
            response.put("message", "Available time added successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else if (result == 0) {
            response.put("message", "Available time already exists or invalid format.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // Or BAD_REQUEST depending on specific validation
        } else { // result == -1 (Doctor not found)
            response.put("message", "Doctor not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Removes a recurring available time slot for a doctor.
     * Secured for the doctor themselves.
     * @param doctorId The ID of the doctor (from path).
     * @param slotId The ID of the DoctorAvailableTime record to delete (from path).
     * @param token The doctor's authentication token.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{doctorId}/available-times/{slotId}/{token}")
    public ResponseEntity<Map<String, String>> removeDoctorAvailableTime(
            @PathVariable Long doctorId, @PathVariable Long slotId, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        Long doctorIdFromToken = tokenService.getUserIdFromToken(token);
        if (doctorIdFromToken == null || !doctorIdFromToken.equals(doctorId) || !centralService.validateToken(token, doctorIdFromToken, "doctor").getStatusCode().equals(HttpStatus.OK)) {
            response.put("message", "Unauthorized to remove available time.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.removeDoctorAvailableTime(slotId, doctorId);
        if (result == 1) {
            response.put("message", "Available time removed successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else { // result == 0 (not found or not owned)
            response.put("message", "Available time not found or you don't have permission to remove it.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
