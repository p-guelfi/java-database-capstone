package com.project.back_end.repository.mysql;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for the Appointment entity.
 * This interface extends JpaRepository to inherit standard CRUD operations for Appointment objects.
 * It also defines custom query methods to support advanced search and filtering based on various criteria.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieves appointments for a specific doctor within a given time range.
     * Uses LEFT JOIN FETCH to eagerly fetch associated Doctor and Patient entities
     * to avoid N+1 problems when accessing their data.
     *
     * @param doctorId The ID of the doctor.
     * @param start The start of the time range (inclusive).
     * @param end The end of the time range (inclusive).
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p WHERE a.doctor.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    /**
     * Filters appointments by doctor ID, a partial patient name (case-insensitive),
     * and a specific time range.
     * Uses LEFT JOIN FETCH to eagerly fetch associated Doctor and Patient entities.
     *
     * @param doctorId The ID of the doctor.
     * @param patientName A partial name of the patient (case-insensitive).
     * @param start The start of the time range (inclusive).
     * @param end The end of the time range (inclusive).
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p WHERE a.doctor.id = :doctorId AND LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%')) AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves all upcoming and CONFIRMED appointments for a specific doctor from a given start time onwards.
     * Includes an optional patient name filter.
     * Uses LEFT JOIN FETCH to eagerly fetch associated Doctor and Patient entities.
     *
     * @param doctorId The ID of the doctor.
     * @param currentDateTime The current date and time (appointments must be after this).
     * @param patientName A partial name of the patient (case-insensitive, can be null for no filter).
     * @return A list of upcoming and confirmed appointments matching the criteria, ordered by appointment time.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p WHERE a.doctor.id = :doctorId AND a.appointmentTime >= :currentDateTime AND a.status = 1 AND (:patientName IS NULL OR LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%'))) ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingByDoctorIdAndPatientName(Long doctorId, LocalDateTime currentDateTime, String patientName);

    /**
     * Deletes all appointments associated with a specific doctor ID.
     * `@Modifying` indicates that this query will modify the database.
     * `@Transactional` ensures the operation is atomic and can be rolled back.
     *
     * @param doctorId The ID of the doctor whose appointments are to be deleted.
     */
    @Modifying // Indicates that this query modifies the database
    @Transactional // Ensures the operation is atomic and can be rolled back
    void deleteAllByDoctorId(Long doctorId);

    /**
     * Finds all appointments for a specific patient.
     * Spring Data JPA automatically generates the query for this method.
     *
     * @param patientId The ID of the patient.
     * @return A list of appointments for the given patient.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieves appointments for a specific patient by their status, ordered by appointment time in ascending order.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param patientId The ID of the patient.
     * @param status The status of the appointments (e.g., 0 for Scheduled, 1 for Completed).
     * @return A list of appointments matching the criteria, ordered by time.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Searches appointments by a partial doctor's name (case-insensitive) and patient ID.
     * Uses a custom JPQL query with LOWER and CONCAT for robust partial matching.
     *
     * @param doctorName A partial name of the doctor (case-insensitive).
     * @param patientId The ID of the patient.
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(String doctorName, Long patientId);

    /**
     * Filters appointments by a partial doctor's name (case-insensitive), patient ID, and status.
     * Uses a custom JPQL query for comprehensive filtering.
     *
     * @param doctorName A partial name of the doctor (case-insensitive).
     * @param patientId The ID of the patient.
     * @param status The status of the appointments.
     * @return A list of appointments matching the criteria.
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, Long patientId, int status);
}
