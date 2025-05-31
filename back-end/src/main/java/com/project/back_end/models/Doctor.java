package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.DayOfWeek; // Import for DayOfWeek enum if you use it for availableDays
import java.util.Set; // Import for Set
import java.util.List; // Keep if you still have other List fields, but for availableTimes, it's now Set

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Specialty is required")
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters")
    private String specialty;

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "Phone is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    @ElementCollection(fetch = FetchType.EAGER) // Often EAGER for small collections like this
    @CollectionTable(name = "doctor_available_times", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "time_slot")
    private Set<String> availableTimes; // CHANGED to Set<String>

    @ElementCollection(fetch = FetchType.EAGER) // New field for available days
    @CollectionTable(name = "doctor_available_days", joinColumns = @JoinColumn(name = "doctor_id"))
    @Enumerated(EnumType.STRING) // Store DayOfWeek enum as String in DB
    @Column(name = "day_of_week")
    private Set<DayOfWeek> availableDays; // ADDED THIS FIELD (assuming DayOfWeek or String)

    // Getters and Setters

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

    public Set<String> getAvailableTimes() { // CHANGED return type
        return availableTimes;
    }

    public void setAvailableTimes(Set<String> availableTimes) { // CHANGED parameter type
        this.availableTimes = availableTimes;
    }

    // ADDED GETTERS AND SETTERS FOR availableDays
    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(Set<DayOfWeek> availableDays) {
        this.availableDays = availableDays;
    }
}