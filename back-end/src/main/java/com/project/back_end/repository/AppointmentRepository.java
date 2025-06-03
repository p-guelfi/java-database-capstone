package com.project.back_end.repository; // Recommended: Create a 'repository' sub-package

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // While not explicitly asked for findById, good practice for single results

/**
 * Repository interface for Appointment entities.
 * Extends JpaRepository for basic CRUD operations and defines custom query methods.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieves appointments for a specific doctor within a given time range.
     * Uses LEFT JOIN FETCH to eagerly fetch doctor and patient details,
     * which can prevent N+1 select problems.
     *
     * @param doctorId The ID of the doctor.
     * @param start The start of the time range (inclusive).
     * @param end The end of the time range (inclusive).
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " + // Fetch doctor details
           "LEFT JOIN FETCH a.patient p " + // Fetch patient details
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    /**
     * Filters appointments by doctor ID, partial patient name (case-insensitive), and time range.
     * Uses LEFT JOIN FETCH to include patient and doctor details.
     *
     * @param doctorId The ID of the doctor.
     * @param patientName The partial name of the patient (case-insensitive).
     * @param start The start of the time range (inclusive).
     * @param end The end of the time range (inclusive).
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH a.patient p " +
           "WHERE a.doctor.id = :doctorId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " + // p.name for patient's name field
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

    /**
     * Deletes all appointments associated with a specific doctor.
     * Requires @Modifying and @Transactional as it's a modifying query.
     *
     * @param doctorId The ID of the doctor whose appointments are to be deleted.
     */
    @Modifying // Indicates that this query will modify the database
    @Transactional // Ensures the operation runs within a transaction
    void deleteAllByDoctorId(Long doctorId);

    /**
     * Finds all appointments for a specific patient.
     * Spring Data JPA generates this query automatically based on method name.
     *
     * @param patientId The ID of the patient.
     * @return A list of appointments for the given patient.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieves appointments for a patient by their status, ordered by appointment time in ascending order.
     * Spring Data JPA generates this query automatically based on method name.
     *
     * @param patientId The ID of the patient.
     * @param status The status of the appointments.
     * @return A list of appointments matching the criteria, ordered by appointment time.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Searches appointments by partial doctor name (case-insensitive) and patient ID.
     *
     * @param doctorName The partial name of the doctor (case-insensitive).
     * @param patientId The ID of the patient.
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH a.patient p " +
           "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " + // d.name for doctor's name field (assuming Doctor has a 'name' field)
           "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(String doctorName, Long patientId);

    /**
     * Filters appointments by partial doctor name (case-insensitive), patient ID, and status.
     *
     * @param doctorName The partial name of the doctor (case-insensitive).
     * @param patientId The ID of the patient.
     * @param status The status of the appointments.
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH a.patient p " +
           "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId " +
           "AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, Long patientId, int status);

    // Optional: If you might need to find by doctor ID only without time range, you could add:
    // List<Appointment> findByDoctorId(Long doctorId);

    // Optional: If you need to find by patient ID and specific time range
    // List<Appointment> findByPatientIdAndAppointmentTimeBetween(Long patientId, LocalDateTime start, LocalDateTime end);
}