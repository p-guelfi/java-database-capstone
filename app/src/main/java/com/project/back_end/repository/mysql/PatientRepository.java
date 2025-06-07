package com.project.back_end.repository.mysql;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Use Optional for methods that might not find a result

/**
 * Repository interface for the Patient entity.
 * This extends JpaRepository to provide standard CRUD operations for Patient objects.
 * It also includes custom query methods for finding patients by email or phone number.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Finds a Patient entity by their email address.
     * Spring Data JPA automatically generates the query for this method based on its name.
     *
     * @param email The email address of the patient to find.
     * @return An Optional containing the Patient if found, or an empty Optional if not found.
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Finds a Patient entity by either their email address or their phone number.
     * Spring Data JPA automatically generates the query for this method based on its name.
     *
     * @param email The email address of the patient.
     * @param phone The phone number of the patient.
     * @return An Optional containing the Patient if found by either email or phone, or an empty Optional if not found.
     */
    Optional<Patient> findByEmailOrPhone(String email, String phone);
}
