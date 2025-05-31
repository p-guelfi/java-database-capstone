package com.project.back_end.repository;

import com.project.back_end.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Admin entities.
 * Extends JpaRepository to inherit basic CRUD operations.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Finds an Admin entity by their username.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param username The username to search for.
     * @return An Optional containing the Admin entity if found, or an empty Optional if not.
     */
    Optional<Admin> findByUsername(String username); // <-- THIS LINE MUST BE EXACTLY LIKE THIS
}