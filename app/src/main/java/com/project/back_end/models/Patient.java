package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty; // Though not explicitly requested for WRITE_ONLY on password, it's good practice.

/**
 * Represents a patient in the Smart Clinic Management System.
 * This entity is mapped to the 'patients' table in the MySQL database.
 * It captures personal details and contact information for appointment booking and treatment tracking.
 */
@Entity // Marks this class as a JPA entity, indicating it maps to a database table.
public class Patient {

    @Id // Designates 'id' as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key to be auto-incremented by the database.
    private Long id; // Unique identifier for the patient.

    @NotNull(message = "Patient's name cannot be null") // Ensures the name field is not null.
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") // Specifies length constraints for the name.
    private String name; // Full name of the patient.

    @NotNull(message = "Email cannot be null") // Ensures the email field is not null.
    @Email(message = "Email should be a valid email format") // Validates the email format.
    @Column(unique = true, nullable = false) // Ensures the email is unique and non-null in the database.
    private String email; // The patient's email address, used for login and contact.

    @NotNull(message = "Password cannot be null") // Ensures the password field is not null.
    @Size(min = 6, message = "Password must be at least 6 characters long") // Specifies minimum length for the password.
    // Optional: @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) if password should not be serialized to JSON responses.
    private String password; // The patient's password for authentication.

    @NotNull(message = "Phone number cannot be null") // Ensures the phone field is not null.
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") // Validates that the phone number is exactly 10 digits.
    @Column(unique = true, nullable = false) // Ensures the phone number is unique and non-null in the database.
    private String phone; // The patient's phone number.

    @NotNull(message = "Address cannot be null") // Ensures the address field is not null.
    @Size(max = 255, message = "Address cannot exceed 255 characters") // Specifies maximum length for the address.
    private String address; // The patient's physical address.

    // Default constructor (required by JPA and Spring Data)
    public Patient() {
    }

    // Constructor for creating new Patient instances with required fields.
    public Patient(String name, String email, String password, String phone, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }

    // --- Getters and Setters for all attributes ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Patient{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               '}';
    }
}
