package com.project.back_end.repository.mysql;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the Doctor entity.
 * Provides CRUD operations and custom query capabilities for Doctor objects.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by email.
     * @param email The email of the doctor.
     * @return An Optional containing the Doctor if found.
     */
    Optional<Doctor> findByEmail(String email);

    /**
     * Finds a doctor by phone number.
     * @param phone The phone number of the doctor.
     * @return An Optional containing the Doctor if found.
     */
    Optional<Doctor> findByPhone(String phone);

    /**
     * Finds a doctor by name (case-insensitive, partial match).
     * Spring Data JPA automatically generates the query.
     * @param name The name or part of the name to search for.
     * @return A list of doctors matching the name.
     */
    List<Doctor> findByNameContainingIgnoreCase(String name);

    /**
     * Finds doctors by specialty (case-insensitive, partial match).
     * @param specialty The specialty or part of the specialty to search for.
     * @return A list of doctors matching the specialty.
     */
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);

    /**
     * Finds doctors by name and specialty (both case-insensitive, partial match).
     * @param name The name or part of the name to search for.
     * @param specialty The specialty or part of the specialty to search for.
     * @return A list of doctors matching both criteria.
     */
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(String name, String specialty);

    // You might also want methods for finding by email OR phone, etc.
}
