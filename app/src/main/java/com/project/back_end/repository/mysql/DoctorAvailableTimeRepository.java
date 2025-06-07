package com.project.back_end.repository.mysql;

import com.project.back_end.models.DoctorAvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Import List for findByDoctor_Id method
import java.util.Optional; // Import Optional

/**
 * Repository interface for the DoctorAvailableTime entity.
 * Provides standard CRUD operations and custom query capabilities for doctor available time slots.
 */
@Repository
public interface DoctorAvailableTimeRepository extends JpaRepository<DoctorAvailableTime, Long> {

    /**
     * Finds all DoctorAvailableTime entries for a specific doctor ID.
     * This is used to retrieve all recurring available time slots for a doctor.
     *
     * @param doctorId The ID of the doctor.
     * @return A list of DoctorAvailableTime objects for the given doctor.
     */
    List<DoctorAvailableTime> findByDoctor_Id(Long doctorId); // NEW: Method to find all slots for a doctor

    /**
     * Finds a DoctorAvailableTime entry by doctor ID and a specific time slot string.
     * This is used to check for existing recurring availability slots for a doctor.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param doctorId The ID of the doctor.
     * @param timeSlot The specific time slot string (e.g., "09:00-10:00").
     * @return An Optional containing the DoctorAvailableTime if found, empty otherwise.
     */
    Optional<DoctorAvailableTime> findByDoctor_IdAndTimeSlot(Long doctorId, String timeSlot);

    /**
     * Deletes a DoctorAvailableTime entry by its ID and ensures it belongs to the specified doctor.
     * This adds an extra layer of security and ensures the correct record is deleted.
     *
     * @param id The ID of the available time slot record to delete.
     * @param doctorId The ID of the doctor who owns this slot.
     * @return The number of entities deleted.
     */
    long deleteByIdAndDoctor_Id(Long id, Long doctorId);
}
