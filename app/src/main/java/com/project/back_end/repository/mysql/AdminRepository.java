package com.project.back_end.repository.mysql;

import com.project.back_end.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Use Optional for methods that might not find a result

/**
 * Repository interface for the Admin entity.
 * This interface extends JpaRepository to inherit standard CRUD (Create, Read, Update, Delete)
 * operations for Admin objects, with Long as the type of the primary key.
 * It also defines custom query methods using Spring Data JPA conventions.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Finds an Admin entity by their username.
     * Spring Data JPA automatically generates the query for this method based on its name.
     * @param username The username of the admin to find.
     * @return An Optional containing the Admin if found, or an empty Optional if not found.
     */
    Optional<Admin> findByUsername(String username);
}
