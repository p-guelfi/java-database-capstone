package com.project.back_end.services; // Your confirmed service package

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project_back_end.DTO.Login;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService; // Injected for token generation (placeholder)

    // Constructor Injection
    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Fetches the available slots for a specific doctor on a given date.
     * Assumes doctor's 'availableTimes' are in "HH:MM" format (e.g., "09:00", "10:00").
     * Assumes appointments are 1 hour long for simplicity in calculating booked slots.
     *
     * @param doctorId The ID of the doctor.
     * @param date The date for which availability is needed.
     * @return A list of available time slots for the doctor on the specified date.
     */
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
        if (doctorOptional.isEmpty()) {
            return Collections.emptyList(); // Doctor not found
        }
        Doctor doctor = doctorOptional.get();

        // Ensure doctor.getAvailableTimes() returns a Set<String>
        Set<String> doctorAvailableSlots = doctor.getAvailableTimes();

        // Get all appointments for this doctor on this specific date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // End of the day
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay);

        Set<String> bookedTimeSlots = bookedAppointments.stream()
                .map(appt -> appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toSet());

        // Filter out booked slots from available slots
        List<String> availableSlots = doctorAvailableSlots.stream()
                .filter(slot -> !bookedTimeSlots.contains(slot))
                .sorted() // Keep slots sorted
                .collect(Collectors.toList());

        return availableSlots;
    }

    /**
     * Saves a new doctor to the database.
     * WARNING: Password is NOT encoded here. This is INSECURE for real applications.
     *
     * @param doctor The doctor object to save.
     * @return 1 for success, -1 if the doctor already exists (by email), 0 for internal errors.
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        // Check if doctor with this email already exists
        Optional<Doctor> existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
        if (existingDoctor.isPresent()) {
            return -1; // Doctor with this email already exists
        }

        try {
            // WARNING: Storing password without encoding is INSECURE.
            // In a real application, you would use passwordEncoder.encode(doctor.getPassword()) here.
            doctorRepository.save(doctor); // Saving password as plain text
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Updates the details of an existing doctor.
     * WARNING: Password update logic is simplified and INSECURE if not used with a PasswordEncoder.
     *
     * @param doctor The doctor object with updated details.
     * @return 1 for success, -1 if doctor not found, 0 for internal errors.
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        // Check if the doctor exists by ID
        Optional<Doctor> existingDoctorOptional = doctorRepository.findById(doctor.getId());
        if (existingDoctorOptional.isEmpty()) {
            return -1; // Doctor not found
        }

        Doctor existingDoctor = existingDoctorOptional.get();

        try {
            // Update fields.
            // WARNING: If password is updated, it's done in plain text. Highly INSECURE.
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty() &&
                !doctor.getPassword().equals(existingDoctor.getPassword())) { // Plain text comparison
                existingDoctor.setPassword(doctor.getPassword()); // Storing new password in plain text
            }

            existingDoctor.setName(doctor.getName());
            existingDoctor.setEmail(doctor.getEmail());
            existingDoctor.setPhone(doctor.getPhone());
            existingDoctor.setSpecialty(doctor.getSpecialty());

            // Ensure availableDays is handled correctly, assuming it's Set<DayOfWeek> in Doctor model
            if (doctor.getAvailableDays() != null) {
                existingDoctor.setAvailableDays(doctor.getAvailableDays());
            }
            // Ensure availableTimes is handled correctly, assuming it's Set<String> in Doctor model
            if (doctor.getAvailableTimes() != null) {
                existingDoctor.setAvailableTimes(doctor.getAvailableTimes());
            }

            doctorRepository.save(existingDoctor);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Retrieves a list of all doctors.
     *
     * @return A list of all doctors.
     */
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor by ID.
     * Also deletes all associated appointments.
     *
     * @param id The ID of the doctor to be deleted.
     * @return 1 for success, -1 if doctor not found, 0 for internal errors.
     */
    @Transactional // Ensures atomicity: delete appointments THEN doctor
    public int deleteDoctor(long id) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);
        if (doctorOptional.isEmpty()) {
            return -1; // Doctor not found
        }

        try {
            // Delete all associated appointments first
            appointmentRepository.deleteAllByDoctorId(id);
            // Then delete the doctor
            doctorRepository.delete(doctorOptional.get());
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error deleting doctor or their appointments: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Validates a doctor's login credentials.
     * IMPORTANT: This version does NOT securely verify the password.
     * It only checks if a doctor exists with the given email.
     *
     * @param login The login object containing email and password.
     * @return ResponseEntity with token if email exists, or an error message.
     */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        Optional<Doctor> doctorOptional = doctorRepository.findByEmail(login.getEmail());
        if (doctorOptional.isEmpty()) {
            response.put("message", "Invalid credentials (Doctor not found by email).");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Doctor doctor = doctorOptional.get();

        // >>>>>>>>>>>>>>>>>> WARNING: INSECURE PASSWORD VERIFICATION <<<<<<<<<<<<<<<<<<<<
        // For the purpose of this lab, we are explicitly skipping secure password comparison.
        // In a real application, you MUST use a PasswordEncoder (e.g., BCryptPasswordEncoder)
        // to hash and compare passwords securely. Example: passwordEncoder.matches(login.getPassword(), doctor.getPassword())
        // For this lab, if the email matches, we consider it "valid" for token generation.
        // If you were to do a plain-text comparison (e.g., doctor.getPassword().equals(login.getPassword())),
        // it would be highly insecure. We are avoiding that specific comparison here.
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> END WARNING <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        // Placeholder for TokenService logic
        String token;
        try {
            token = tokenService.generateToken(doctor.getId(), "ROLE_DOCTOR");
        } catch (UnsupportedOperationException e) {
            response.put("message", "Token generation service not fully implemented yet.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("message", "Failed to generate token.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "Login successful (password not securely verified).");
        response.put("token", token);
        response.put("userId", doctor.getId().toString());
        response.put("role", "doctor");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Finds doctors by their name (partial and case-insensitive match).
     *
     * @param name The name of the doctor to search for.
     * @return A map with the list of doctors matching the name.
     */
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        // CHANGED: Using findByNameLike as defined in DoctorRepository
        List<Doctor> doctors = doctorRepository.findByNameLike(name);

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found with that name.");
        } else {
            response.put("message", "Doctors retrieved successfully.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by name, specialty, and availability during AM/PM.
     *
     * @param name Doctor's name (optional).
     * @param specialty Doctor's specialty (optional).
     * @param amOrPm Time of day: "AM" or "PM" (optional).
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findAll(); // Start with all doctors

        if (name != null && !name.trim().isEmpty()) {
            doctors = doctors.stream()
                             .filter(d -> d.getName().toLowerCase().contains(name.trim().toLowerCase()))
                             .collect(Collectors.toList());
        }
        if (specialty != null && !specialty.trim().isEmpty()) {
            doctors = doctors.stream()
                             .filter(d -> d.getSpecialty().equalsIgnoreCase(specialty.trim()))
                             .collect(Collectors.toList());
        }
        if (amOrPm != null && !amOrPm.trim().isEmpty()) {
            doctors = filterDoctorByTime(doctors, amOrPm); // Use private helper method
        }

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found matching the criteria.");
        } else {
            response.put("message", "Doctors filtered successfully.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by name and their availability during AM/PM.
     *
     * @param name Doctor's name.
     * @param amOrPm Time of day: "AM" or "PM".
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        // CHANGED: Using findByNameLike as defined in DoctorRepository
        List<Doctor> doctors = doctorRepository.findByNameLike(name); // First filter by name
        doctors = filterDoctorByTime(doctors, amOrPm); // Then filter by time

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found matching the name and time criteria.");
        } else {
            response.put("message", "Doctors filtered successfully by name and time.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by name and specialty.
     *
     * @param name Doctor's name.
     * @param specialty Doctor's specialty.
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();
        // Assuming DoctorRepository has this derived query method
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found matching the name and specialty criteria.");
        } else {
            response.put("message", "Doctors filtered successfully by name and specialty.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by specialty and their availability during AM/PM.
     *
     * @param specialty Doctor's specialty.
     * @param amOrPm Time of day: "AM" or "PM".
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty); // First filter by specialty
        doctors = filterDoctorByTime(doctors, amOrPm); // Then filter by time

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found matching the specialty and time criteria.");
        } else {
            response.put("message", "Doctors filtered successfully by specialty and time.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by specialty.
     *
     * @param specialty Doctor's specialty.
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found with that specialty.");
        } else {
            response.put("message", "Doctors filtered successfully by specialty.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Filters doctors by their availability during AM/PM.
     *
     * @param amOrPm Time of day: "AM" or "PM".
     * @return A map with the filtered list of doctors.
     */
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findAll();
        doctors = filterDoctorByTime(doctors, amOrPm); // Use private helper method

        if (doctors.isEmpty()) {
            response.put("message", "No doctors found available at that time of day.");
        } else {
            response.put("message", "Doctors filtered successfully by time.");
        }
        response.put("doctors", doctors);
        return response;
    }

    /**
     * Private helper method to filter a list of doctors by their available times (AM/PM).
     * Assumes available times in Doctor are strings like "HH:MM".
     *
     * @param doctors The list of doctors to filter.
     * @param amOrPm Time of day: "AM" or "PM".
     * @return A filtered list of doctors.
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.trim().isEmpty()) {
            return doctors; // No filter applied if amOrPm is null/empty
        }

        final LocalTime NOON = LocalTime.of(12, 0); // Define noon once

        return doctors.stream()
                .filter(doctor -> {
                    // Handle potential null availableTimes to prevent NullPointerException
                    if (doctor.getAvailableTimes() == null) {
                        return false;
                    }
                    return doctor.getAvailableTimes().stream().anyMatch(slot -> {
                        try {
                            LocalTime slotTime = LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm"));
                            if ("AM".equalsIgnoreCase(amOrPm)) {
                                return slotTime.isBefore(NOON); // Before 12:00 PM
                            } else if ("PM".equalsIgnoreCase(amOrPm)) {
                                return !slotTime.isBefore(NOON); // 12:00 PM and after
                            }
                            return false; // Invalid amOrPm value
                        } catch (DateTimeParseException e) {
                            System.err.println("Warning: Invalid time format in Doctor availableTimes for doctor ID " + doctor.getId() + ": " + slot);
                            return false; // Skip invalid time formats
                        }
                    });
                })
                .collect(Collectors.toList());
    }
}