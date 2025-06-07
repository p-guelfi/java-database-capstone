package com.project.back_end.service;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.DoctorAvailableTime; // New import
import com.project.back_end.repository.mysql.DoctorRepository;
import com.project.back_end.repository.mysql.DoctorAvailableTimeRepository; // New import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // New import for LocalDate
import java.util.List;
import java.util.Optional;

/**
 * Service class for handling business logic related to Doctor entities.
 * Includes operations for managing doctor profiles and their recurring availability.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailableTimeRepository availableTimeRepository; // New: Inject AvailableTime Repository

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, DoctorAvailableTimeRepository availableTimeRepository) {
        this.doctorRepository = doctorRepository;
        this.availableTimeRepository = availableTimeRepository; // Autowire
    }

    /**
     * Finds a doctor by ID.
     * @param id The ID of the doctor.
     * @return An Optional containing the Doctor if found, empty otherwise.
     */
    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    /**
     * Updates a doctor's profile.
     * @param updatedDoctor The Doctor object with updated fields.
     * @return 1 for success, -1 if doctor not found, 0 for other errors.
     */
    @Transactional
    public int updateDoctor(Doctor updatedDoctor) {
        Optional<Doctor> existingDoctorOpt = doctorRepository.findById(updatedDoctor.getId());
        if (!existingDoctorOpt.isPresent()) {
            System.err.println("Error updating doctor: Doctor not found with ID " + updatedDoctor.getId());
            return -1; // Doctor not found
        }

        Doctor existingDoctor = existingDoctorOpt.get();

        // Update fields that are allowed to be changed via this profile update
        if (updatedDoctor.getName() != null && !updatedDoctor.getName().isEmpty()) {
            existingDoctor.setName(updatedDoctor.getName());
        }
        if (updatedDoctor.getEmail() != null && !updatedDoctor.getEmail().isEmpty()) {
            existingDoctor.setEmail(updatedDoctor.getEmail());
        }
        if (updatedDoctor.getPhone() != null && !updatedDoctor.getPhone().isEmpty()) {
            existingDoctor.setPhone(updatedDoctor.getPhone());
        }
        if (updatedDoctor.getSpecialty() != null && !updatedDoctor.getSpecialty().isEmpty()) {
            existingDoctor.setSpecialty(updatedDoctor.getSpecialty());
        }
        // IMPORTANT: Do NOT update password here unless specific password change logic is implemented

        try {
            doctorRepository.save(existingDoctor);
            return 1; // Successfully updated
        } catch (Exception e) {
            System.err.println("Error saving updated doctor: " + e.getMessage());
            return 0; // Generic error during save
        }
    }

    /**
     * Retrieves all recurring available time slots for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of DoctorAvailableTime objects.
     */
    public List<DoctorAvailableTime> getDoctorAvailableTimes(Long doctorId) {
        return availableTimeRepository.findByDoctor_Id(doctorId);
    }

    /**
     * Adds a new recurring available time slot for a doctor.
     * @param doctorId The ID of the doctor.
     * @param availableTimesStr The time slot string (e.g., "09:00-10:00").
     * @return 1 for success, -1 if doctor not found, 0 if slot already exists or other error.
     */
    @Transactional
    public int addDoctorAvailableTime(Long doctorId, String availableTimesStr) { // Changed parameter name
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return -1; // Doctor not found
        }
        Doctor doctor = doctorOpt.get();

        // Check if this specific slot already exists for this doctor
        // CORRECTED METHOD CALL: findByDoctor_IdAndAvailableTimes
        if (availableTimeRepository.findByDoctor_IdAndAvailableTimes(doctorId, availableTimesStr).isPresent()) {
            System.out.println("DEBUG: DoctorService - Slot " + availableTimesStr + " already exists for doctor " + doctorId);
            return 0; // Slot already exists
        }

        DoctorAvailableTime newSlot = new DoctorAvailableTime(doctor, availableTimesStr); // Use corrected parameter name
        try {
            availableTimeRepository.save(newSlot);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error adding available time: " + e.getMessage());
            return 0; // Error during save
        }
    }

    /**
     * Removes a recurring available time slot for a doctor.
     * @param slotId The ID of the DoctorAvailableTime record to remove.
     * @param doctorId The ID of the doctor to ensure ownership.
     * @return 1 for success, 0 if not found or error.
     */
    @Transactional
    public int removeDoctorAvailableTime(Long slotId, Long doctorId) {
        // Ensure the slot belongs to the doctor trying to delete it
        long deletedCount = availableTimeRepository.deleteByIdAndDoctor_Id(slotId, doctorId);
        if (deletedCount > 0) {
            return 1; // Successfully deleted
        } else {
            System.err.println("Error removing available time: Slot ID " + slotId + " not found for doctor " + doctorId + " or not owned.");
            return 0; // Not found or not owned
        }
    }

    /**
     * Fetches a list of all doctors from the backend. (Public view)
     * @return {Array<Object>} - An array of doctor objects, or an empty array if an error occurs.
     */
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor by their ID. Requires an authentication token. (Admin function)
     * @param id - The ID of the doctor to delete.
     */
    @Transactional
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    /**
     * Saves (adds) a doctor's record. (Admin function)
     * @param doctor - The doctor object to save.
     * @return The saved Doctor object.
     */
    @Transactional
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    /**
     * Filters doctors based on name, time availability, and specialty. (Public view)
     * @param name - Doctor's name (optional).
     * @param time - Available time slot (optional).
     * @param specialty - Doctor's specialty (optional).
     * @return A filtered list of doctor objects.
     */
    public List<Doctor> filterDoctors(String name, String time, String specialty) {
        // TODO: Implement actual filtering logic here based on your requirements
        // For now, it returns all doctors as a placeholder.
        return doctorRepository.findAll();
    }

    /**
     * Retrieves available times for a doctor for a specific date.
     * IMPORTANT: This method currently returns recurring available times.
     * For actual date-specific availability (considering appointments/unavailability),
     * you would need to implement more complex logic here.
     * @param doctorId The ID of the doctor.
     * @param date The specific date for which to retrieve availability.
     * @return A list of DoctorAvailableTime objects for the given date.
     */
    public List<DoctorAvailableTime> getDoctorAvailability(Long doctorId, LocalDate date) {
        // For now, return all recurring available times.
        // In a real application, you'd filter these by date,
        // and potentially remove times blocked by appointments or specific unavailability.
        System.out.println("DEBUG: getDoctorAvailability called for doctor " + doctorId + " on " + date);
        return availableTimeRepository.findByDoctor_Id(doctorId);
    }
}
