package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project_back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.AppointmentRepository; // ADDED THIS IMPORT
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import com.project.back_end.repository.PrescriptionRepository; // ADDED THIS IMPORT
import com.project.back_end.services.TokenService; // ENSURE THIS IS PRESENT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime; // This import might not be strictly needed if only used for formatting
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService; // Keep if AppService delegates to DoctorService
    private final PatientService patientService; // Keep if AppService delegates to PatientService
    private final AppointmentRepository appointmentRepository; // Ensure this is declared if used in constructor
    private final PrescriptionRepository prescriptionRepository; // Ensure this is declared if used in constructor


    // CORRECTED CONSTRUCTOR NAME AND PARAMETER LIST
    public AppService(TokenService tokenService,
                      AdminRepository adminRepository,
                      DoctorRepository doctorRepository,
                      PatientRepository patientRepository,
                      DoctorService doctorService, // This should be injected if used in AppService
                      PatientService patientService, // This should be injected if used in AppService
                      AppointmentRepository appointmentRepository, // If used in AppService
                      PrescriptionRepository prescriptionRepository) { // If used in AppService
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentRepository = appointmentRepository; // Assign it
        this.prescriptionRepository = prescriptionRepository; // Assign it
    }

    // This is the added method!
    public TokenService getTokenService() {
        return tokenService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String userRole, Long userId) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isValid = tokenService.validateToken(token, userId, userRole);
            if (!isValid) {
                response.put("message", "Invalid or expired token for this user and role.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token validation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Token validation failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        Optional<Admin> existingAdmin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (existingAdmin.isEmpty()) {
            response.put("message", "Invalid credentials: Admin not found.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Admin admin = existingAdmin.get();

        try {
            String token = tokenService.generateToken(admin.getId(), "ADMIN");
            response.put("message", "Admin login successful.");
            response.put("token", token);
            response.put("userId", admin.getId().toString());
            response.put("role", "admin");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token generation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Failed to generate token for admin: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        Optional<Patient> patientOptional = patientRepository.findByEmail(login.getEmail());

        if (patientOptional.isEmpty()) {
            response.put("message", "Invalid credentials: Patient not found by email.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Patient patient = patientOptional.get();

        try {
            String token = tokenService.generateToken(patient.getId(), "PATIENT");
            response.put("message", "Patient login successful.");
            response.put("token", token);
            response.put("userId", patient.getId().toString());
            response.put("role", "patient");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token generation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Failed to generate token for patient: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return 0;
        }

        Optional<Doctor> doctorOptional = doctorRepository.findById(appointment.getDoctor().getId());
        if (doctorOptional.isEmpty()) {
            return -1;
        }

        // Assuming appointment.getAppointmentTime() is a LocalDateTime
        String appointmentTimeSlot = appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();

        try {
            // This calls DoctorService, so DoctorService needs to be injected into AppService
            List<String> availableSlots = doctorService.getDoctorAvailability(
                appointment.getDoctor().getId(),
                appointmentDate
            );

            if (availableSlots.contains(appointmentTimeSlot)) {
                return 1;
            } else {
                return 0;
            }
        } catch (DateTimeParseException e) {
            return -2;
        } catch (Exception e) {
            return 0; // Generic error
        }
    }

    public boolean validatePatient(Patient patient) {
        Optional<Patient> existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existingPatient.isEmpty();
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();

        Long patientId;
        try {
            patientId = tokenService.getUserIdFromToken(token);
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token service not fully implemented for user ID extraction.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token. Could not extract patient ID.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // Delegating to patientService for actual filtering logic
        if ((condition != null && !condition.trim().isEmpty()) && (name != null && !name.trim().isEmpty())) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (condition != null && !condition.trim().isEmpty()) {
            return patientService.filterByCondition(condition, patientId);
        } else if (name != null && !name.trim().isEmpty()) {
            return patientService.filterByDoctor(name, patientId);
        } else {
            // If no filters, return all appointments for the patient
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}