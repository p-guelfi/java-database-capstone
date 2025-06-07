package com.project.back_end.service;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.mysql.AppointmentRepository;
import com.project.back_end.repository.mysql.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for handling various operations related to patients.
 * This includes creating a patient, fetching their appointments,
 * and filtering those appointments based on specific conditions.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService; // Used for token extraction and validation

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Saves a new patient to the database.
     *
     * @param patient The patient object to be saved.
     * @return 1 on success, 0 on failure (e.g., exception, patient with same email/phone already exists).
     */
    @Transactional
    public int createPatient(Patient patient) {
        // Check if a patient with the same email or phone already exists
        if (patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()).isPresent()) {
            System.err.println("Error creating patient: Patient with email " + patient.getEmail() +
                    " or phone " + patient.getPhone() + " already exists.");
            return 0; // Indicate failure due to duplicate
        }
        try {
            patientRepository.save(patient);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving patient: " + e.getMessage());
            return 0; // Internal error during save
        }
    }

    /**
     * Retrieves a list of appointments for a specific patient.
     * The method validates if the provided patient ID matches the one associated with the token.
     *
     * @param id    The patient's ID.
     * @param token The JWT token for authorization.
     * @return A response containing a list of appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        // Assuming TokenService can give us the patient ID directly from the token
        // In a real JWT, you'd decode the token to get the user ID from its claims
        Long patientIdFromToken = tokenService.getUserIdFromToken(token);

        if (patientIdFromToken == null || !patientIdFromToken.equals(id)) {
            response.put("message", "Unauthorized: Token does not match patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
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
                        appt.getStatus(),
                        appt.getNotes()
                ))
                .collect(Collectors.toList());

        response.put("appointments", appointmentDTOs);
        response.put("message", "Appointments retrieved successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters appointments by condition (past or future) for a specific patient.
     *
     * @param condition The condition to filter by ("past" or "future").
     * @param id        The patient’s ID.
     * @return A response containing the filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        Long patientIdFromToken = tokenService.getUserIdFromToken(token);
        if (patientIdFromToken == null || !patientIdFromToken.equals(id)) {
            response.put("message", "Unauthorized: Token does not match patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<Appointment> filteredAppointments;
        LocalDateTime now = LocalDateTime.now();

        // Status: 0 = Scheduled (future), 1 = Completed (past), 2 = Cancelled
        if ("past".equalsIgnoreCase(condition)) {
            // Retrieve appointments with status 'Completed' (1) and those scheduled in the past.
            filteredAppointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 1);
            // Optionally, also include scheduled appointments whose time has passed
            List<Appointment> pastScheduled = appointmentRepository.findByPatientId(id).stream()
                    .filter(a -> a.getStatus() == 0 && a.getAppointmentTime().isBefore(now))
                    .collect(Collectors.toList());
            filteredAppointments.addAll(pastScheduled);
            // Ensure no duplicates if an appointment might be in both categories due to nuanced status handling
            filteredAppointments = filteredAppointments.stream().distinct().collect(Collectors.toList());

        } else if ("future".equalsIgnoreCase(condition)) {
            // Retrieve appointments with status 'Scheduled' (0) that are in the future.
            filteredAppointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 0).stream()
                    .filter(a -> a.getAppointmentTime().isAfter(now))
                    .collect(Collectors.toList());
        } else {
            response.put("message", "Invalid condition. Please use 'past' or 'future'.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<AppointmentDTO> appointmentDTOs = filteredAppointments.stream()
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

        response.put("appointments", appointmentDTOs);
        response.put("message", "Appointments filtered successfully by condition.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters the patient's appointments by doctor's name.
     *
     * @param name      The name of the doctor.
     * @param patientId The ID of the patient.
     * @return A response containing the filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();

        Long patientIdFromToken = tokenService.getUserIdFromToken(token);
        if (patientIdFromToken == null || !patientIdFromToken.equals(patientId)) {
            response.put("message", "Unauthorized: Token does not match patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<Appointment> filteredAppointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);

        List<AppointmentDTO> appointmentDTOs = filteredAppointments.stream()
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

        response.put("appointments", appointmentDTOs);
        response.put("message", "Appointments filtered successfully by doctor name.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Filters the patient's appointments by doctor's name and appointment condition (past or future).
     *
     * @param condition The condition to filter by ("past" or "future").
     * @param name      The name of the doctor.
     * @param patientId The ID of the patient.
     * @return A response containing the filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();

        Long patientIdFromToken = tokenService.getUserIdFromToken(token);
        if (patientIdFromToken == null || !patientIdFromToken.equals(patientId)) {
            response.put("message", "Unauthorized: Token does not match patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<Appointment> filteredAppointments;
        LocalDateTime now = LocalDateTime.now();
        int statusFilter = -1; // -1 means no status filter applied initially

        if ("past".equalsIgnoreCase(condition)) {
            statusFilter = 1; // Completed
        } else if ("future".equalsIgnoreCase(condition)) {
            statusFilter = 0; // Scheduled
        } else {
            response.put("message", "Invalid condition. Please use 'past' or 'future'.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (statusFilter != -1) {
            filteredAppointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, statusFilter);
        } else {
            // This case should ideally not be reached if condition is strictly "past" or "future"
            filteredAppointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
        }

        // Apply time-based filtering for "past" and "future" more strictly
        filteredAppointments = filteredAppointments.stream()
                .filter(a -> {
                    if ("past".equalsIgnoreCase(condition)) {
                        return a.getAppointmentTime().isBefore(now);
                    } else if ("future".equalsIgnoreCase(condition)) {
                        return a.getAppointmentTime().isAfter(now);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<AppointmentDTO> appointmentDTOs = filteredAppointments.stream()
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

        response.put("appointments", appointmentDTOs);
        response.put("message", "Appointments filtered successfully by doctor and condition.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Fetches the patient's details based on the provided JWT token.
     * The token is assumed to contain the patient's email or ID in its claims.
     *
     * @param token The JWT token for authorization.
     * @return A response containing the patient's details or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();

        // Assuming TokenService can extract the patient ID from the token
        Long patientIdFromToken = tokenService.getUserIdFromToken(token);

        if (patientIdFromToken == null) {
            response.put("message", "Invalid or missing patient ID in token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Optional<Patient> patientOpt = patientRepository.findById(patientIdFromToken);

        if (!patientOpt.isPresent()) {
            response.put("message", "Patient not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientOpt.get();
        // Return patient details directly (e.g., as a map or a patient DTO)
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("id", patient.getId());
        patientDetails.put("name", patient.getName());
        patientDetails.put("email", patient.getEmail());
        patientDetails.put("phone", patient.getPhone());
        patientDetails.put("address", patient.getAddress());

        response.put("patient", patientDetails);
        response.put("message", "Patient details retrieved successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
