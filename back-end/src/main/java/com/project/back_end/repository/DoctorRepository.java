package com.project.back_end.repository; // Recommended: Create a 'repository' sub-package

import com.project.back_end.models.Doctor; // Correct import for your Doctor entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Good practice for findByEmail

/**
 * Repository interface for Doctor entities.
 * Extends JpaRepository for basic CRUD operations and defines custom query methods.
 */
@Repository // Marks this interface as a Spring Data JPA repository component
public interface DoctorRepository extends JpaRepository<Doctor, Long> { // Doctor entity, Long ID type

    /**
     * Finds a Doctor entity by their email address.
     * Spring Data JPA automatically generates this query based on the method name.
     * Optional is used as the doctor might not be found.
     *
     * @param email The email address to search for.
     * @return An Optional containing the Doctor if found, or an empty Optional otherwise.
     */
    Optional<Doctor> findByEmail(String email);

    /**
     * Finds doctors by a partial name match.
     * Uses a custom JPQL query with LIKE and CONCAT for flexible pattern matching.
     *
     * @param name The partial name to search for.
     * @return A list of doctors matching the partial name.
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(String name);

    /**
     * Filters doctors by partial name and exact specialty, both case-insensitive.
     * Uses a custom JPQL query with LOWER, CONCAT, and LIKE for case-insensitive matching.
     *
     * @param name The partial name to search for (case-insensitive).
     * @param specialty The specialty to filter by (case-insensitive).
     * @return A list of doctors matching the criteria.
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

    /**
     * Finds doctors by their specialty, ignoring case.
     * Spring Data JPA automatically generates this query based on the method name.
     *
     * @param specialty The specialty to search for.
     * @return A list of doctors with the specified specialty.
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}