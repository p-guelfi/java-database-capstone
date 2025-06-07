package com.project.back_end.service;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.dto.LoginDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.models.DoctorAvailableTime; // Import for DoctorAvailableTime
import com.project.back_end.repository.mysql.AdminRepository;
import com.project.back_end.repository.mysql.DoctorRepository;
import com.project.back_end.repository.mysql.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A central service class that consolidates various business functionalities
 * including authentication, doctor and patient management, and appointment validation.
 * It coordinates operations across different repositories and other services.
 */
@Service("centralService") // Give it a specific bean name to avoid potential conflicts with 'Service' interface if any
public class CentralService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService; // Autowire DoctorService
    private final PatientService patientService; // Autowire PatientService

    @Autowired
    public CentralService(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validates the authenticity and expiration of a given JWT token for a specific user role.
     * This method acts as a gatekeeper for protected resources.
     *
     * @param token The JWT token to be validated.
     * @param userId The ID of the user (Admin, Doctor, or Patient).
     * @param user  The user role expected to own this token (e.g., "admin", "doctor", "patient").
     * @return ResponseEntity with an error message if the token is invalid or unauthorized,
     * or an OK status if the token is valid for the user.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, Long userId, String user) {
        Map<String, String> response = new HashMap<>();

        if (token == null || !tokenService.validateTokenForUser(token, userId, user)) {
            response.put("message", "Unauthorized: Invalid or expired token for " + user + ".");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.put("message", "Token is valid.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Validates the login credentials of an admin. If successful, it generates and returns a JWT token.
     *
     * @param receivedAdmin The admin credentials (username and password) from the login request.
     * @return ResponseEntity containing a generated token on success, or an error message on failure.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        Optional<Admin> adminOpt = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (!adminOpt.isPresent()) {
            response.put("message", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Admin admin = adminOpt.get();

        // In a real application, you would hash the password and compare hashes.
        // For this exercise, we are doing a direct password comparison.
        if (admin.getPassword().equals(receivedAdmin.getPassword())) {
            // Generate a real JWT token for the admin
            String token = tokenService.generateToken(admin.getUsername(), admin.getId(), "admin");
            response.put("message", "Admin login successful!");
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Filters doctors based on name, specialty, and available time (AM/PM).
     * This method delegates to the DoctorService for the actual filtering logic.
     *
     * @param name      The name of the doctor (optional).
     * @param specialty The specialty of the doctor (optional).
     * @param time      The available time of the doctor ("AM", "PM", or null/empty).
     * @return A map containing a list of doctors that match the filtering criteria.
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        // Delegate complex filtering logic to DoctorService
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorService.filterDoctors(name, time, specialty);
        result.put("doctors", doctors);
        result.put("message", "Doctors filtered successfully.");
        return result;
    }

    /**
     * Validates whether an appointment time slot is available for a specific doctor.
     * It checks if the doctor exists and if the proposed time slot is not already booked.
     *
     * @param appointment The appointment object to validate (should contain doctor ID and proposed time).
     * @return 1 if the appointment time is valid, 0 if the time is unavailable, -1 if the doctor doesn't exist.
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1; // Doctor ID is missing
        }
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
        if (!doctorOpt.isPresent()) {
            return -1; // Doctor doesn't exist
        }

        // Use DoctorService to check availability for the specific time
        // We need to extract the date from the proposed appointment time
        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();

        // Get the available slots for the doctor on that specific date
        // The getDoctorAvailability method already returns a List<DoctorAvailableTime>
        List<DoctorAvailableTime> doctorAvailableSlots = doctorService.getDoctorAvailability(appointment.getDoctor().getId(), appointmentDate);

        // Convert the proposed appointment time to a slot string format for comparison
        String proposedSlot = String.format("%02d:%02d-%02d:%02d", // Adjusted format to match "HH:MM-HH:MM"
                appointment.getAppointmentTime().getHour(), appointment.getAppointmentTime().getMinute(),
                appointment.getAppointmentTime().plusHours(1).getHour(), appointment.getAppointmentTime().plusHours(1).getMinute());

        // Check if the proposed slot is actually one of the doctor's generally available slots AND not already booked
        // You need to extract the 'timeSlot' string from DoctorAvailableTime objects for comparison
        boolean isSlotGenerallyAvailable = doctorAvailableSlots.stream()
            .anyMatch(slot -> slot.getTimeSlot().equals(proposedSlot)); // CHANGED: Use getTimeSlot()

        if (isSlotGenerallyAvailable) {
            // Also, double-check that the appointment time is in the future.
            if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
                System.err.println("Validation Error: Appointment time is in the past.");
                return 0; // Past time is unavailable
            }
            return 1; // Time is valid and available
        } else {
            System.err.println("Validation Error: Proposed slot " + proposedSlot + " is not available for doctor " + appointment.getDoctor().getId() + " on " + appointmentDate);
            return 0; // Time is unavailable (either not generally available or already booked)
        }
    }

    /**
     * Validates whether a patient with the given email or phone number already exists in the system.
     * Used to prevent duplicate registrations.
     *
     * @param patient The patient object to validate (contains email and phone).
     * @return true if the patient does NOT exist (meaning it's a valid new patient), false if the patient already exists.
     */
    public boolean validatePatient(Patient patient) {
        // Check if a patient with the same email or phone already exists
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()).isEmpty();
    }

    /**
     * Validates a doctor's login credentials (email and password).
     * If valid, it generates and returns an authentication token for the doctor.
     *
     * @param loginDTO The LoginDTO containing the doctor's email and password.
     * @return ResponseEntity containing a generated token on successful login, or an error message on failure.
     */
    public ResponseEntity<Map<String, String>> validateDoctorLogin(LoginDTO loginDTO) {
        Map<String, String> response = new HashMap<>();

        System.out.println("DEBUG: Doctor Login Attempt - Email: " + loginDTO.getEmail() + ", Password: " + loginDTO.getPassword());

        Optional<Doctor> doctorOpt = doctorRepository.findByEmail(loginDTO.getEmail());

        if (!doctorOpt.isPresent()) {
            System.out.println("DEBUG: Doctor with email " + loginDTO.getEmail() + " not found.");
            response.put("message", "Doctor not found with this email.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Doctor doctor = doctorOpt.get();
        System.out.println("DEBUG: Doctor found - ID: " + doctor.getId() + ", Name: " + doctor.getName());
        System.out.println("DEBUG: Stored Password (DB): " + doctor.getPassword());

        // IMPORTANT: In a real app, hash passwords and compare hashes.
        // For this exercise, direct string comparison is used.
        if (doctor.getPassword().equals(loginDTO.getPassword())) {
            System.out.println("DEBUG: Password match. Generating token.");
            String token = tokenService.generateToken(doctor.getEmail(), doctor.getId(), "doctor");
            response.put("message", "Doctor login successful!");
            response.put("token", token);
            // Optionally, return doctor ID if needed by frontend
            response.put("userId", doctor.getId().toString()); // Use userId for consistency with localStorage
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            System.out.println("DEBUG: Password mismatch.");
            response.put("message", "Invalid password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Validates a patient's login credentials (email and password).
     * If valid, it generates and returns an authentication token for the patient.
     *
     * @param loginDTO The LoginDTO containing the patient's email and password.
     * @return ResponseEntity containing a generated token on successful login, or an error message on failure.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(LoginDTO loginDTO) { // Changed parameter name to loginDTO
        Map<String, String> response = new HashMap<>();
        Optional<Patient> patientOpt = patientRepository.findByEmail(loginDTO.getEmail()); // Use loginDTO

        if (!patientOpt.isPresent()) {
            response.put("message", "Patient not found with this email.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }

        Patient patient = patientOpt.get();

        // In a real application, you would hash the password and compare hashes.
        // For this exercise, we are doing a direct password comparison.
        if (patient.getPassword().equals(loginDTO.getPassword())) { // Use loginDTO
            // Generate a real JWT token for the patient
            String token = tokenService.generateToken(patient.getEmail(), patient.getId(), "patient");
            response.put("message", "Patient login successful!");
            response.put("token", token);
            // Optionally, also return patient ID if the frontend needs it for direct API calls
            // response.put("patientId", patient.getId().toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Invalid password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
        }
    }

    /**
     * Filters patient appointments based on various criteria such as condition (past/future) and doctor name.
     * This method delegates to the PatientService for the actual filtering logic.
     *
     * @param condition The medical condition or status to filter appointments by ("past", "future").
     * @param name      The doctor's name to filter appointments by (optional).
     * @param token     The authentication token to identify the patient.
     * @return ResponseEntity containing the filtered list of patient appointments (as DTOs) or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        // First, get patient ID from token for authorization
        Long patientId = tokenService.getUserIdFromToken(token);
        if (patientId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unauthorized: Invalid or missing patient ID in token.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Delegate to PatientService based on combination of filters
        if (condition != null && !condition.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId, token);
        } else if (condition != null && !condition.trim().isEmpty()) {
            return patientService.filterByCondition(condition, patientId, token);
        } else if (name != null && !name.trim().isEmpty()) {
            return patientService.filterByDoctor(name, patientId, token);
        } else {
            // If no filters are provided, return all appointments for the patient
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}
