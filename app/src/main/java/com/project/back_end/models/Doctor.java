package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection; // For List<String> availableTimes
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList; // Good practice to initialize collections

/**
 * Represents a healthcare provider (Doctor) in the Smart Clinic Management System.
 * This entity is mapped to the 'doctors' table in the MySQL database.
 * It stores doctor's personal and professional details, including availability.
 */
@Entity // Marks this class as a JPA entity, indicating it maps to a database table.
public class Doctor {

    @Id // Designates 'id' as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key to be auto-incremented by the database.
    private Long id; // Unique identifier for the doctor.

    @NotNull(message = "Doctor's name cannot be null") // Ensures the name field is not null.
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") // Specifies length constraints for the name.
    private String name; // Full name of the doctor.

    @NotNull(message = "Specialty cannot be null") // Ensures the specialty field is not null.
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters") // Specifies length constraints for the specialty.
    private String specialty; // The medical specialty of the doctor.

    @NotNull(message = "Email cannot be null") // Ensures the email field is not null.
    @Email(message = "Email should be a valid email format") // Validates the email format.
    @Column(unique = true, nullable = false) // Ensures the email is unique and non-null in the database.
    private String email; // The doctor's email address, used for login and contact.

    @NotNull(message = "Password cannot be null") // Ensures the password field is not null.
    @Size(min = 6, message = "Password must be at least 6 characters long") // Specifies minimum length for the password.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hides the password from JSON responses but allows it in incoming JSON requests.
    private String password; // The doctor's password for authentication.

    @NotNull(message = "Phone number cannot be null") // Ensures the phone field is not null.
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") // Validates that the phone number is exactly 10 digits.
    @Column(unique = true, nullable = false) // Ensures the phone number is unique and non-null in the database.
    private String phone; // The doctor's phone number.

    @ElementCollection // Indicates that 'availableTimes' is a collection of basic types or embeddable classes.
    // By default, this will create a separate table (e.g., doctor_available_times) to store the strings.
    private List<String> availableTimes = new ArrayList<>(); // List of time slots when the doctor is available.

    // Default constructor (required by JPA and Spring Data)
    public Doctor() {
    }

    // Constructor for creating new Doctor instances (excluding ID and availableTimes for initial creation)
    public Doctor(String name, String specialty, String email, String password, String phone) {
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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

    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }

    @Override
    public String toString() {
        return "Doctor{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", specialty='" + specialty + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               '}';
    }
}
