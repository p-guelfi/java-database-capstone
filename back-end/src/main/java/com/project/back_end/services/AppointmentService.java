package com.project.back_end.services; // Your confirmed service package

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service // Marks this class as a Spring Service component
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService; // Declaring TokenService

    // Constructor Injection
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService) { // TokenService injected here
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /**
     * Books a new appointment.
     * Includes basic validation for doctor and patient existence, and time overlap.
     *
     * @param appointment The appointment object to book.
     * @return 1 if successful, 0 if there's an error.
     */
    @Transactional // Ensures the operation is atomic
    public int bookAppointment(Appointment appointment) {
        // 1. Validate Doctor and Patient existence
        Optional<Doctor> doctorOptional = doctorRepository.findById(appointment.getDoctor().getId());
        Optional<Patient> patientOptional = patientRepository.findById(appointment.getPatient().getId());

        if (doctorOptional.isEmpty()) {
            System.err.println("Error: Doctor not found with ID: " + appointment.getDoctor().getId());
            return 0; // Doctor not found
        }
        if (patientOptional.isEmpty()) {
            System.err.println("Error: Patient not found with ID: " + appointment.getPatient().getId());
            return 0; // Patient not found
        }

        // Set managed entities to avoid transient object issues
        appointment.setDoctor(doctorOptional.get());
        appointment.setPatient(patientOptional.get());

        // 2. Validate Appointment Time (Check for overlaps for this doctor)
        LocalDateTime requestedTime = appointment.getAppointmentTime();
        LocalDateTime endTime = appointment.getEndTime(); // Assuming 1 hour appointment based on entity getEndTime()

        // Find existing appointments for this doctor that overlap with the requested time
        List<Appointment> overlappingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(
                        appointment.getDoctor().getId(),
                        requestedTime.minusMinutes(59), // Check for overlaps (e.g., if appointment ends before new one begins by a minute)
                        endTime.plusMinutes(59) // Adjust based on exact overlap logic needed
                );

        // Filter out appointments that truly overlap (basic check)
        boolean hasOverlap = overlappingAppointments.stream()
            .anyMatch(existingAppt -> {
                // Check if existingAppt's time range overlaps with requested Appt's time range
                return !(endTime.isBefore(existingAppt.getAppointmentTime()) ||
                         requestedTime.isAfter(existingAppt.getEndTime()));
            });

        if (hasOverlap) {
            System.err.println("Error: Doctor is not available at the requested time due to an overlap.");
            return 0; // Doctor not available (overlap)
        }

        try {
            appointmentRepository.save(appointment);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            return 0; // Generic error
        }
    }

    /**
     * Updates an existing appointment.
     *
     * @param appointment The appointment object to update.
     * @return A response message indicating success or failure.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        // 1. Check if the appointment to update exists
        Optional<Appointment> existingAppointmentOptional = appointmentRepository.findById(appointment.getId());
        if (existingAppointmentOptional.isEmpty()) {
            response.put("message", "Appointment not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment existingAppointment = existingAppointmentOptional.get();

        // 2. Validate Doctor and Patient existence if their IDs are changed (or just retrieve latest data)
        Optional<Doctor> doctorOptional = doctorRepository.findById(appointment.getDoctor().getId());
        Optional<Patient> patientOptional = patientRepository.findById(appointment.getPatient().getId());

        if (doctorOptional.isEmpty()) {
            response.put("message", "Invalid Doctor ID.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (patientOptional.isEmpty()) {
            response.put("message", "Invalid Patient ID.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Set managed entities to avoid transient object issues for the update
        appointment.setDoctor(doctorOptional.get());
        appointment.setPatient(patientOptional.get());

        // 3. Validate new appointment time for overlaps for the doctor
        LocalDateTime requestedTime = appointment.getAppointmentTime();
        LocalDateTime endTime = appointment.getEndTime();

        List<Appointment> overlappingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(
                        appointment.getDoctor().getId(),
                        requestedTime.minusMinutes(59),
                        endTime.plusMinutes(59)
                );

        boolean hasOverlap = overlappingAppointments.stream()
            .anyMatch(existingAppt -> !existingAppt.getId().equals(appointment.getId()) && // Exclude the appointment being updated itself
                                    !(endTime.isBefore(existingAppt.getAppointmentTime()) ||
                                      requestedTime.isAfter(existingAppt.getEndTime())));

        if (hasOverlap) {
            response.put("message", "Doctor is unavailable at the requested new time.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // HTTP 409 Conflict
        }

        try {
            // Update the fields of the existing appointment
            existingAppointment.setDoctor(appointment.getDoctor());
            existingAppointment.setPatient(appointment.getPatient());
            existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
            existingAppointment.setStatus(appointment.getStatus());

            appointmentRepository.save(existingAppointment);
            response.put("message", "Appointment updated successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Failed to update appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancels an existing appointment.
     * Ensures that the patient attempting to cancel is the one who originally booked it.
     *
     * @param id The ID of the appointment to cancel.
     * @param token The authorization token of the user attempting to cancel.
     * @return A response message indicating success or failure.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        if (appointmentOptional.isEmpty()) {
            response.put("message", "Appointment not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment appointmentToCancel = appointmentOptional.get();

        // --- Placeholder for TokenService logic ---
        // In a real application, you would use tokenService to get the ID of the authenticated user
        Long authenticatedPatientId;
        try {
            authenticatedPatientId = tokenService.getUserIdFromToken(token);
            // You might also check tokenService.getUserRoleFromToken(token) to ensure it's a patient
        } catch (UnsupportedOperationException e) {
            // This means TokenService.getUserIdFromToken is not yet implemented
            response.put("message", "Token validation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Check if the authenticated user is the patient who booked the appointment
        if (!authenticatedPatientId.equals(appointmentToCancel.getPatient().getId())) {
            response.put("message", "Unauthorized to cancel this appointment.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // HTTP 403 Forbidden
        }
        // --- End Placeholder ---

        try {
            appointmentRepository.delete(appointmentToCancel);
            response.put("message", "Appointment cancelled successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Failed to cancel appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a list of appointments for a specific doctor on a specific date,
     * optionally filtered by patient name.
     *
     * @param patientName Patient name to filter by (can be null or empty).
     * @param date The date for appointments.
     * @param token The authorization token of the doctor.
     * @return A map containing the list of appointments.
     */
    public Map<String, Object> getAppointment(String patientName, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        List<Appointment> appointments;

        // --- Placeholder for TokenService logic ---
        // In a real application, you would use tokenService to get the ID of the authenticated doctor
        Long authenticatedDoctorId;
        try {
            authenticatedDoctorId = tokenService.getUserIdFromToken(token); // Assuming this returns the doctor's ID
            // You might also check tokenService.getUserRoleFromToken(token) to ensure it's a doctor
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token validation service not fully implemented yet.");
            response.put("appointments", Collections.emptyList());
            return response;
        } catch (Exception e) {
            response.put("message", "Invalid or expired token.");
            response.put("appointments", Collections.emptyList());
            return response;
        }
        // --- End Placeholder ---

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // End of the day

        if (patientName != null && !patientName.trim().isEmpty()) {
            // Filter by doctor ID, patient name, and time range
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    authenticatedDoctorId, patientName.trim(), startOfDay, endOfDay);
        } else {
            // Filter by doctor ID and time range only
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    authenticatedDoctorId, startOfDay, endOfDay);
        }

        if (appointments.isEmpty()) {
            response.put("message", "No appointments found for the specified criteria.");
        } else {
            response.put("message", "Appointments retrieved successfully.");
        }
        response.put("appointments", appointments);
        return response;
    }

    /**
     * Helper method to validate appointment time for overlaps.
     * This logic is integrated directly into bookAppointment and updateAppointment for now.
     * You might extract this into a separate private method if the validation logic becomes complex.
     *
     * @param newAppointment The new or updated appointment to validate.
     * @return true if valid (no overlaps), false otherwise.
     */
    // private boolean validateAppointment(Appointment newAppointment) {
    //     // This was hinted in the update method. The logic is now inline in book/updateAppointment methods.
    //     // You can extract it here if preferred.
    //     return true;
    // }
}