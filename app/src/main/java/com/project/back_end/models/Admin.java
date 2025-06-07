package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column; // Import Column for explicit column mapping to enforce unique
import jakarta.validation.constraints.NotNull; // For @NotNull annotation
import com.fasterxml.jackson.annotation.JsonProperty; // For @JsonProperty annotation

/**
 * Represents a system administrator in the Smart Clinic Management System.
 * This entity is mapped to the 'admin' table in the MySQL database.
 * Admins manage high-level operations such as user access, data review, and system maintenance.
 * This model contains basic login credentials required to authenticate an admin.
 */
@Entity // Marks this class as a JPA entity, indicating it maps to a database table.
public class Admin {

    @Id // Designates 'id' as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key to be auto-incremented by the database.
    private Long id; // Unique identifier for the admin.

    @NotNull(message = "Username cannot be null") // Ensures the username field is not null during validation.
    @Column(unique = true, nullable = false) // Maps 'username' to a database column; ensures uniqueness and non-nullability at DB level.
    private String username; // The admin's username, used for authentication.

    @NotNull(message = "Password cannot be null") // Ensures the password field is not null during validation.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hides the password from JSON responses but allows it in incoming JSON requests.
    private String password; // The admin's password, used for authentication.

    // Default constructor (required by JPA and Spring Data)
    public Admin() {
    }

    // Constructor to easily create new Admin instances with required fields.
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters and Setters for all attributes ---

    /**
     * Retrieves the unique ID of the admin.
     * @return The admin's ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the admin.
     * While typically auto-generated, a setter can be useful for testing or specific scenarios.
     * @param id The ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the username of the admin.
     * @return The admin's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the admin.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the password of the admin.
     * IMPORTANT: Due to @JsonProperty(access = JsonProperty.Access.WRITE_ONLY),
     * this getter's return value will NOT be included when an Admin object is
     * serialized into a JSON response.
     * @return The admin's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the admin.
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Provides a string representation of the Admin object, excluding the password for security.
     * @return A string representation of the Admin.
     */
    @Override
    public String toString() {
        return "Admin{" +
               "id=" + id +
               ", username='" + username + '\'' +
               '}';
    }
}
