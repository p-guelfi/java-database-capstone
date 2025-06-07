package com.project.back_end.service;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.mysql.AppointmentRepository;
import com.project.back_end.repository.mysql.DoctorRepository;
import com.project.back_end.repository.mysql.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for handling all business logic related to appointments.
 * This includes booking, updating, canceling, and retrieving appointments,
 * ensuring data integrity and business rules are applied.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService; // Used for token validation and user ID extraction

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /**
     * Books a new appointment in the system.
     * Validates doctor and patient existence and appointment time availability.
     *
     * @param appointment The Appointment object to book. It should contain references to Doctor and Patient.
     * @return 1 if the appointment is successfully booked, 0 if there's an error (e.g., doctor/patient not found, time slot taken).
     */
    @Transactional // Ensures the entire operation is atomic
    public int bookAppointment(Appointment appointment) {
        // 1. Validate Doctor and Patient existence
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
        Optional<Patient> patientOpt = patientRepository.findById(appointment.getPatient().getId());

        if (!doctorOpt.isPresent()) {
            System.err.println("Error booking appointment: Doctor not found with ID " + appointment.getDoctor().getId());
            return 0; // Doctor not found
        }
        if (!patientOpt.isPresent()) {
            System.err.println("Error booking appointment: Patient not found with ID " + appointment.getPatient().getId());
            return 0; // Patient not found
        }

        // Set managed entities to the appointment object
        appointment.setDoctor(doctorOpt.get());
        appointment.setPatient(patientOpt.get());

        // 2. Validate appointment time availability for the specific doctor
        // Check if any existing appointment overlaps with the new one for this doctor
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime(),
                appointment.getAppointmentTime().plusHours(1).minusMinutes(1) // Check for overlaps within 1 hour slot
        );

        if (!existingAppointments.isEmpty()) {
            System.err.println("Error booking appointment: Doctor " + appointment.getDoctor().getName() +
                    " is already booked at " + appointment.getAppointmentTime());
            return 0; // Time slot already taken
        }

        try {
            appointmentRepository.save(appointment);
            return 1; // Successfully booked
        } catch (Exception e) {
            System.err.println("Error saving appointment: " + e.getMessage());
            return 0; // Generic error during save
        }
    }

    /**
     * Updates an existing appointment.
     *
     * @param updatedAppointment The Appointment object with updated details.
     * @return ResponseEntity indicating success or failure.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment updatedAppointment) {
        Map<String, String> response = new HashMap<>();

        // 1. Find the existing appointment
        Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(updatedAppointment.getId());
        if (!existingAppointmentOpt.isPresent()) {
            response.put("message", "Appointment not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment existingAppointment = existingAppointmentOpt.get();

        // 2. Validate if doctor or patient IDs are being changed (if so, find new entities)
        if (updatedAppointment.getDoctor() != null && !updatedAppointment.getDoctor().getId().equals(existingAppointment.getDoctor().getId())) {
            Optional<Doctor> newDoctorOpt = doctorRepository.findById(updatedAppointment.getDoctor().getId());
            if (!newDoctorOpt.isPresent()) {
                response.put("message", "Invalid new Doctor ID provided.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            existingAppointment.setDoctor(newDoctorOpt.get());
        }
        if (updatedAppointment.getPatient() != null && !updatedAppointment.getPatient().getId().equals(existingAppointment.getPatient().getId())) {
            Optional<Patient> newPatientOpt = patientRepository.findById(updatedAppointment.getPatient().getId());
            if (!newPatientOpt.isPresent()) {
                response.put("message", "Invalid new Patient ID provided.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            existingAppointment.setPatient(newPatientOpt.get());
        }

        // 3. Validate appointment time availability for the potentially new doctor
        if (updatedAppointment.getAppointmentTime() != null && !updatedAppointment.getAppointmentTime().equals(existingAppointment.getAppointmentTime())) {
            List<Appointment> overlappingAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    existingAppointment.getDoctor().getId(), // Use potentially new doctor if changed
                    updatedAppointment.getAppointmentTime(),
                    updatedAppointment.getAppointmentTime().plusHours(1).minusMinutes(1)
            );
            // Ensure no overlap with other appointments (excluding itself if time is only slightly adjusted)
            boolean hasOverlap = overlappingAppointments.stream()
                    .anyMatch(a -> !a.getId().equals(existingAppointment.getId()));

            if (hasOverlap) {
                response.put("message", "The new appointment time overlaps with an existing appointment for the doctor.");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT); // Use CONFLICT for time slot issues
            }
            existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        }

        // 4. Update other fields if provided
        if (updatedAppointment.getStatus() != 0) { // Assuming 0 is a default/uninitialized status
            existingAppointment.setStatus(updatedAppointment.getStatus());
        }
        if (updatedAppointment.getNotes() != null) {
            existingAppointment.setNotes(updatedAppointment.getNotes());
        }

        try {
            appointmentRepository.save(existingAppointment);
            response.put("message", "Appointment updated successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            response.put("message", "Failed to update appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancels an existing appointment.
     * This method ensures that the patient attempting to cancel is the one who booked it.
     *
     * @param appointmentId The ID of the appointment to cancel.
     * @param token The authorization token of the user attempting to cancel.
     * @return ResponseEntity indicating success or failure.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            response.put("message", "Appointment not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Appointment appointment = appointmentOpt.get();

        // Validate that the token belongs to the patient who booked the appointment
        Long patientIdFromToken = tokenService.getUserIdFromToken(token); // Get ID from token
        if (patientIdFromToken == null || !tokenService.validateTokenForUser(token, patientIdFromToken, "patient")) { // Updated call
            response.put("message", "Unauthorized to cancel this appointment. Token is invalid or does not match patient.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // HTTP 403 Forbidden
        }
        if (!patientIdFromToken.equals(appointment.getPatient().getId())) {
             response.put("message", "Unauthorized to cancel this appointment. Only the booking patient can cancel.");
             return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }


        try {
            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            response.put("message", "Failed to cancel appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a list of appointments for a specific doctor on a specific date,
     * with optional filtering by patient name. This method now specifically
     * handles requests that provide a definite 'date' filter.
     *
     * @param patientName Patient name to filter by (optional, can be null or empty).
     * @param date The specific date for appointments.
     * @param token The authorization token of the doctor.
     * @return A Map containing the list of appointments (converted to DTOs) and potentially other metadata.
     */
    public Map<String, Object> getAppointments(String patientName, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();
        List<Appointment> appointments;

        Long doctorId = tokenService.getUserIdFromToken(token); // Extract doctor ID from token
        if (doctorId == null || !tokenService.validateTokenForUser(token, doctorId, "doctor")) { // Updated call
            result.put("message", "Invalid or missing doctor ID in token, or token invalid for doctor role.");
            result.put("appointments", List.of()); // Return empty list
            return result;
        }

        // Define the start and end of the day for the given date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        if (patientName != null && !patientName.trim().isEmpty()) {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, patientName, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, startOfDay, endOfDay);
        }

        // --- START DEBUG LOGGING ---
        System.out.println("DEBUG: Appointments retrieved from repository (before DTO mapping): " + appointments.size());
        if (!appointments.isEmpty()) {
            appointments.forEach(appt -> {
                System.out.println("DEBUG: Appointment ID: " + appt.getId());
                System.out.println("DEBUG:   Status: " + appt.getStatus());
                System.out.println("DEBUG:   Notes: " + (appt.getNotes() == null ? "NULL" : appt.getNotes().isEmpty() ? "EMPTY STRING" : appt.getNotes()));
                if (appt.getPatient() != null) {
                    System.out.println("DEBUG:   Patient Object (NOT NULL):");
                    System.out.println("DEBUG:     Patient ID: " + appt.getPatient().getId());
                    System.out.println("DEBUG:     Patient Name: " + (appt.getPatient().getName() == null ? "NULL" : appt.getPatient().getName()));
                    System.out.println("DEBUG:     Patient Email: " + (appt.getPatient().getEmail() == null ? "NULL" : appt.getPatient().getEmail()));
                    System.out.println("DEBUG:     Patient Phone: " + (appt.getPatient().getPhone() == null ? "NULL" : appt.getPatient().getPhone()));
                } else {
                    System.out.println("DEBUG:   Patient Object IS NULL.");
                }
            });
        }
        // --- END DEBUG LOGGING ---


        // Convert Appointment entities to AppointmentDTOs
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appt -> new AppointmentDTO(
                        appt.getId(),
                        appt.getDoctor().getId(),
                        appt.getDoctor().getName(),
                        appt.getPatient().getId(), // This will throw NPE if appt.getPatient() is null
                        appt.getPatient().getName(), // This will throw NPE if appt.getPatient() is null
                        appt.getPatient().getEmail(), // This will throw NPE if appt.getPatient() is null
                        appt.getPatient().getPhone(), // This will throw NPE if appt.getPatient() is null
                        appt.getPatient().getAddress(),
                        appt.getAppointmentTime(),
                        appt.getStatus(),
                        appt.getNotes() // Pass the notes field to the DTO constructor
                ))
                .collect(Collectors.toList());

        result.put("appointments", appointmentDTOs);
        result.put("message", "Appointments retrieved successfully.");
        return result;
    }

    /**
     * Retrieves all upcoming appointments for a specific doctor, with optional filtering by patient name.
     * "Upcoming" means appointments from the current exact time onwards.
     *
     * @param patientName Patient name to filter by (optional, can be null or empty).
     * @param token The authorization token of the doctor.
     * @return A Map containing the list of appointments (converted to DTOs) and a message.
     */
    public Map<String, Object> getUpcomingAppointments(String patientName, String token) {
        Map<String, Object> result = new HashMap<>();
        List<Appointment> appointments;

        Long doctorId = tokenService.getUserIdFromToken(token);
        if (doctorId == null || !tokenService.validateTokenForUser(token, doctorId, "doctor")) {
            result.put("message", "Invalid or missing doctor ID in token, or token invalid for doctor role.");
            result.put("appointments", List.of());
            return result;
        }

        // Get current LocalDateTime for "upcoming" filter
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Use the new repository method for upcoming appointments
        String finalPatientName = (patientName != null && !patientName.trim().isEmpty()) ? patientName : null;
        appointments = appointmentRepository.findUpcomingByDoctorIdAndPatientName(doctorId, currentDateTime, finalPatientName);

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
                        appt.getStatus(),
                        appt.getNotes()
                ))
                .collect(Collectors.toList());

        result.put("appointments", appointmentDTOs);
        result.put("message", "Upcoming appointments retrieved successfully.");
        return result;
    }


    /**
     * Helper method to retrieve all appointments for a specific patient.
     * This might be used by a patient controller.
     * @param patientId The ID of the patient.
     * @param token The token for authorization.
     * @return List of AppointmentDTOs for the patient.
     */
    public List<AppointmentDTO> getAppointmentsForPatient(Long patientId, String token) {
        if (!tokenService.validateTokenForUser(token, patientId, "patient")) { // Updated call
            System.err.println("Token does not match patient ID or role: " + patientId);
            return List.of(); // Return empty list if unauthorized
        }

        // This method also needs to ensure eager fetching if it's not already doing so
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);

        return appointments.stream()
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
                        appt.getStatus(),
                        appt.getNotes() // Pass the notes field to the DTO constructor
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves appointments for a specific patient by status.
     * @param patientId The ID of the patient.
     * @param status The status to filter by (e.g., 0 for Scheduled).
     * @param token The token for authorization.
     * @return List of AppointmentDTOs for the patient filtered by status.
     */
    public List<AppointmentDTO> getAppointmentsForPatientByStatus(Long patientId, int status, String token) {
        if (!tokenService.validateTokenForUser(token, patientId, "patient")) { // Updated call
            System.err.println("Token does not match patient ID or role for status filter: " + patientId);
            return List.of();
        }
        // This method also needs to ensure eager fetching if it's not already doing so
        List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
        return appointments.stream()
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
                        appt.getStatus(),
                        appt.getNotes() // Pass the notes field to the DTO constructor
                ))
                .collect(Collectors.toList());
    }
}
