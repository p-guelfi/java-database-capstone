package com.project.back_end.repository.mysql;

import com.project.back_end.models.DoctorAvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the DoctorAvailableTime entity.
 * Provides CRUD operations and custom query capabilities for doctor's recurring available time slots.
 */
@Repository
public interface DoctorAvailableTimeRepository extends JpaRepository<DoctorAvailableTime, Long> {

    /**
     * Finds all DoctorAvailableTime records for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of DoctorAvailableTime records.
     */
    List<DoctorAvailableTime> findByDoctor_Id(Long doctorId);

    /**
     * Finds a specific available time slot for a doctor.
     * Useful for checking if a slot already exists before adding.
     * Corrected method name to match 'availableTimes' property.
     * @param doctorId The ID of the doctor.
     * @param availableTimes The specific time slot string (e.g., "09:00-10:00").
     * @return An Optional containing the DoctorAvailableTime if found.
     */
    Optional<DoctorAvailableTime> findByDoctor_IdAndAvailableTimes(Long doctorId, String availableTimes); // CORRECTED METHOD NAME

    /**
     * Deletes a specific available time slot record for a doctor by its ID.
     * @param id The ID of the DoctorAvailableTime record to delete.
     * @param doctorId The ID of the doctor (for security/verification).
     * @return The number of records deleted.
     */
    long deleteByIdAndDoctor_Id(Long id, Long doctorId);
}
