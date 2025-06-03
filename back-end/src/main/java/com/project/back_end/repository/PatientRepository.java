package com.project.back_end.repository; // Recommended: Create a 'repository' sub-package

import com.project.back_end.models.Patient; // Correct import for your Patient entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Best practice for single result find methods

/**
 * Repository interface for Patient entities.
 * Extends JpaRepository for basic CRUD operations and defines custom query methods.
 */
@Repository // Marks this interface as a Spring Data JPA repository component
public interface PatientRepository extends JpaRepository<Patient, Long> { // Patient entity, Long ID type

    /**
     * Finds a Patient entity by their email address.
     * Spring Data JPA automatically generates this query based on the method name.
     * Optional is used as the patient might not be found.
     *
     * @param email The email address to search for.
     * @return An Optional containing the Patient if found, or an empty Optional otherwise.
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Finds a Patient entity using either their email address or phone number.
     * Spring Data JPA automatically generates this query based on the method name.
     * Optional is used as the patient might not be found.
     *
     * @param email The email address to search for.
     * @param phone The phone number to search for.
     * @return An Optional containing the Patient if found by either email or phone, or an empty Optional otherwise.
     */
    Optional<Patient> findByEmailOrPhone(String email, String phone);
}