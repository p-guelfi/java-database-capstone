package com.project.back_end.services;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService; // Will be implemented later

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Saves a new patient to the database.
     * WARNING: Password is NOT encoded here. This is INSECURE for real applications.
     *
     * @param patient The patient object to be saved.
     * @return Returns 1 on success, and 0 on failure (e.g., exception).
     */
    @Transactional
    public int createPatient(Patient patient) {
        // You might want to add a check here if a patient with the same email already exists.
        Optional<Patient> existingPatient = patientRepository.findByEmail(patient.getEmail());
        if (existingPatient.isPresent()) {
            System.err.println("Error: Patient with email " + patient.getEmail() + " already exists.");
            return 0; // Or a specific error code like -1 for duplicate email
        }

        try {
            // WARNING: Storing password without encoding is INSECURE.
            // In a real application, you would use passwordEncoder.encode(patient.getPassword()) here.
            patientRepository.save(patient); // Saving password as plain text
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving patient: " + e.getMessage());
            return 0; // Generic error
        }
    }

    /**
     * Retrieves a list of appointments for a specific patient.
     * The method checks if the provided patient ID matches the one decoded from the token (by email).
     *
     * @param id The patient's ID.
     * @param token The JWT token containing the email.
     * @return A response containing a list of appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        // --- Placeholder for TokenService logic ---
        // In a real application, you would use tokenService to get the email/ID of the authenticated user
        String authenticatedUserEmail;
        Long authenticatedUserId;
        try {
            authenticatedUserEmail = tokenService.getUserEmailFromToken(token); // Assuming this method exists
            authenticatedUserId = tokenService.getUserIdFromToken(token); // Assuming this method exists and returns patient ID
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token validation service not fully implemented yet.");
            response.put("appointments", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        // --- End Placeholder ---

        // Verify that the requested ID matches the authenticated user's ID
        if (!id.equals(authenticatedUserId)) { // Compare requested ID with ID from token
            response.put("message", "Unauthorized to access appointments for this patient ID.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // HTTP 403 Forbidden
        }

        Optional<Patient> patientOptional = patientRepository.findById(id);
        if (patientOptional.isEmpty()) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentRepository.findByPatientId(id);

        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getDoctor().getId(),
                        appt.getDoctor().getName(),
                        appt.getPatient().getId(),
                        appt.getPatient().getName(),
                        appt.getPatient().getEmail(),
                        appt.getPatient().getPhone(),
                        appt.getPatient().getAddress(),
                        appt.getAppointmentTime(),
                        appt.getStatus()
                ))
                .collect(Collectors.toList());

        if (appointmentDTOs.isEmpty()) {
            response.put("message", "No appointments found for this patient.");
        } else {
            response.put("message", "Appointments retrieved successfully.");
        }
        response.put("appointments", appointmentDTOs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters appointments by condition (past or future) for a specific patient.
     * Uses status: 0 for future/scheduled, 1 for past/completed.
     *
     * @param condition The condition to filter by ("past" or "future").
     * @param patientId The patient’s ID.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Patient> patientOptional = patientRepository.findById(patientId);
        if (patientOptional.isEmpty()) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments;
        int statusToFilter;

        switch (condition.toLowerCase()) {
            case "past":
                statusToFilter = 1; // Assuming 1 means "completed" or "past"
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, statusToFilter);
                // Additionally filter by actual time for past appointments (optional, if status isn't strictly past)
                appointments = appointments.stream()
                                .filter(appt -> appt.getAppointmentTime().isBefore(LocalDateTime.now()))
                                .collect(Collectors.toList());
                break;
            case "future":
                statusToFilter = 0; // Assuming 0 means "scheduled" or "future"
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, statusToFilter);
                // Additionally filter by actual time for future appointments (optional, if status isn't strictly future)
                appointments = appointments.stream()
                                .filter(appt -> appt.getAppointmentTime().isAfter(LocalDateTime.now()))
                                .collect(Collectors.toList());
                break;
            default:
                response.put("message", "Invalid condition. Please use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getDoctor().getId(),
                        appt.getDoctor().getName(),
                        appt.getPatient().getId(),
                        appt.getPatient().getName(),
                        appt.getPatient().getEmail(),
                        appt.getPatient().getPhone(),
                        appt.getPatient().getAddress(),
                        appt.getAppointmentTime(),
                        appt.getStatus()
                ))
                .collect(Collectors.toList());

        if (appointmentDTOs.isEmpty()) {
            response.put("message", "No " + condition + " appointments found for this patient.");
        } else {
            response.put("message", condition + " appointments retrieved successfully.");
        }
        response.put("appointments", appointmentDTOs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters the patient's appointments by doctor's name.
     *
     * @param name The name of the doctor.
     * @param patientId The ID of the patient.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Patient> patientOptional = patientRepository.findById(patientId);
        if (patientOptional.isEmpty()) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);

        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getDoctor().getId(),
                        appt.getDoctor().getName(),
                        appt.getPatient().getId(),
                        appt.getPatient().getName(),
                        appt.getPatient().getEmail(),
                        appt.getPatient().getPhone(),
                        appt.getPatient().getAddress(),
                        appt.getAppointmentTime(),
                        appt.getStatus()
                ))
                .collect(Collectors.toList());

        if (appointmentDTOs.isEmpty()) {
            response.put("message", "No appointments found for doctor '" + name + "'.");
        } else {
            response.put("message", "Appointments filtered by doctor name retrieved successfully.");
        }
        response.put("appointments", appointmentDTOs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters the patient's appointments by doctor's name and appointment condition (past or future).
     *
     * @param condition The condition to filter by ("past" or "future").
     * @param name The name of the doctor.
     * @param patientId The ID of the patient.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Patient> patientOptional = patientRepository.findById(patientId);
        if (patientOptional.isEmpty()) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments;
        int statusToFilter;

        switch (condition.toLowerCase()) {
            case "past":
                statusToFilter = 1; // Assuming 1 means "completed" or "past"
                appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, statusToFilter);
                appointments = appointments.stream()
                                .filter(appt -> appt.getAppointmentTime().isBefore(LocalDateTime.now()))
                                .collect(Collectors.toList());
                break;
            case "future":
                statusToFilter = 0; // Assuming 0 means "scheduled" or "future"
                appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, statusToFilter);
                appointments = appointments.stream()
                                .filter(appt -> appt.getAppointmentTime().isAfter(LocalDateTime.now()))
                                .collect(Collectors.toList());
                break;
            default:
                response.put("message", "Invalid condition. Please use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getDoctor().getId(),
                        appt.getDoctor().getName(),
                        appt.getPatient().getId(),
                        appt.getPatient().getName(),
                        appt.getPatient().getEmail(),
                        appt.getPatient().getPhone(),
                        appt.getPatient().getAddress(),
                        appt.getAppointmentTime(),
                        appt.getStatus()
                ))
                .collect(Collectors.toList());

        if (appointmentDTOs.isEmpty()) {
            response.put("message", "No " + condition + " appointments found for doctor '" + name + "'.");
        } else {
            response.put("message", condition + " appointments for doctor '" + name + "' retrieved successfully.");
        }
        response.put("appointments", appointmentDTOs);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Fetches the patient's details based on the provided JWT token.
     *
     * @param token The JWT token containing the email.
     * @return The patient's details or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();

        // --- Placeholder for TokenService logic ---
        String authenticatedUserEmail;
        try {
            authenticatedUserEmail = tokenService.getUserEmailFromToken(token); // Assuming this method exists
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token validation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        // --- End Placeholder ---

        Optional<Patient> patientOptional = patientRepository.findByEmail(authenticatedUserEmail);

        if (patientOptional.isEmpty()) {
            response.put("message", "Patient details not found for the given token.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientOptional.get();
        // For security, you might want to create a PatientDTO here and exclude sensitive info like password
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("id", patient.getId());
        patientDetails.put("name", patient.getName());
        patientDetails.put("email", patient.getEmail());
        patientDetails.put("phone", patient.getPhone());
        patientDetails.put("address", patient.getAddress());

        response.put("message", "Patient details retrieved successfully.");
        response.put("patient", patientDetails); // Return patient details as a map
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}