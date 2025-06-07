package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Represents a recurring available time slot for a doctor on any given day.
 * This entity is mapped to the 'doctor_available_times' table in the MySQL database.
 */
@Entity
@Table(name = "doctor_available_times")
public class DoctorAvailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Assuming you have an ID column in doctor_available_times, typically auto-incremented

    @NotNull(message = "Doctor cannot be null")
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetch as we mostly care about the time slot itself
    @JoinColumn(name = "doctor_id", nullable = false) // Foreign key to the Doctor table
    private Doctor doctor; // The doctor associated with this available time

    @NotNull(message = "Available time cannot be null")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$",
             message = "Available time must be in HH:MM-HH:MM format (e.g., 09:00-10:00)")
    // REMOVED @Column(name = "available_times")
    private String availableTimes; // Renamed: The time slot string, e.g., "09:00-10:00"

    // Default constructor (required by JPA)
    public DoctorAvailableTime() {
    }

    // Constructor with fields
    public DoctorAvailableTime(Doctor doctor, String availableTimes) {
        this.doctor = doctor;
        this.availableTimes = availableTimes;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public String getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(String availableTimes) {
        this.availableTimes = availableTimes;
    }

    @Override
    public String toString() {
        return "DoctorAvailableTime{" +
               "id=" + id +
               ", doctorId=" + (doctor != null ? doctor.getId() : "null") +
               ", availableTimes='" + availableTimes + '\'' +
               '}';
    }
}
