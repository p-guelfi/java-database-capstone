package com.project.back_end.service;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
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

        String appointmentTimeSlot = appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();

        try {
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
            return 0;
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

        if ((condition != null && !condition.trim().isEmpty()) && (name != null && !name.trim().isEmpty())) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (condition != null && !condition.trim().isEmpty()) {
            return patientService.filterByCondition(condition, patientId);
        } else if (name != null && !name.trim().isEmpty()) {
            return patientService.filterByDoctor(name, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}