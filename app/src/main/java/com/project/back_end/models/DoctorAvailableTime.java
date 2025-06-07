package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a recurring available time slot for a doctor.
 * This entity is mapped to the 'doctor_available_times' table in the MySQL database.
 */
@Entity
@Table(name = "doctor_available_times")
public class DoctorAvailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the available time slot

    @NotNull(message = "Doctor cannot be null")
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetch to avoid unnecessary joins when just listing times
    @JoinColumn(name = "doctor_id", nullable = false) // Foreign key to the Doctor table
    private Doctor doctor; // The doctor associated with this available time

    @Column(name = "time_slot") // Correctly map to the 'time_slot' column
    private String timeSlot; // The recurring time slot (e.g., "09:00-10:00")

    // The 'available_times' column from your DB might be legacy or unused in the entity,
    // but based on your DDL, you have both. We will focus on 'timeSlot'.
    // If 'available_times' was intended to hold the same data, you should align the DDL and entity.
    // @Column(name = "available_times")
    // private String availableTimes; // This field might be redundant if timeSlot holds the value

    // Default constructor (required by JPA)
    public DoctorAvailableTime() {
    }

    // Constructor with doctor and timeSlot
    public DoctorAvailableTime(Doctor doctor, String timeSlot) { // Changed parameter to timeSlot
        this.doctor = doctor;
        this.timeSlot = timeSlot; // Assign to timeSlot
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

    public String getTimeSlot() { // NEW GETTER for the 'time_slot' column
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) { // NEW SETTER for the 'time_slot' column
        this.timeSlot = timeSlot;
    }

    // If 'available_times' column exists and is still used, keep its getter/setter
    // Otherwise, it can be removed from the entity if it's not needed by your application logic.
    // public String getAvailableTimes() {
    //     return availableTimes;
    // }
    // public void setAvailableTimes(String availableTimes) {
    //     this.availableTimes = availableTimes;
    // }

    @Override
    public String toString() {
        return "DoctorAvailableTime{" +
               "id=" + id +
               ", doctorId=" + (doctor != null ? doctor.getId() : "null") +
               ", timeSlot='" + timeSlot + '\'' + // Changed to timeSlot
               '}';
    }
}
