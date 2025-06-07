package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled meeting between a doctor and a patient in the Smart Clinic Management System.
 * This entity is mapped to the 'appointments' table in the MySQL database.
 * It links Doctor and Patient entities and includes appointment specific metadata.
 */
@Entity // Marks this class as a JPA entity, indicating it maps to a database table.
public class Appointment {

    @Id // Designates 'id' as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key to be auto-incremented by the database.
    private Long id; // Unique identifier for the appointment.

    @NotNull(message = "Doctor cannot be null") // Ensures that an appointment must be associated with a doctor.
    @ManyToOne // Defines a many-to-one relationship with the Doctor entity (many appointments to one doctor).
    @JoinColumn(name = "doctor_id", nullable = false) // Specifies the foreign key column in the 'appointments' table that references 'doctors' table.
    private Doctor doctor; // The doctor assigned to this appointment.

    @NotNull(message = "Patient cannot be null") // Ensures that an appointment must be associated with a patient.
    @ManyToOne // Defines a many-to-one relationship with the Patient entity (many appointments to one patient).
    @JoinColumn(name = "patient_id", nullable = false) // Specifies the foreign key column in the 'appointments' table that references 'patients' table.
    private Patient patient; // The patient for whom this appointment is scheduled.

    @NotNull(message = "Appointment time cannot be null") // Ensures the appointment time is provided.
    @Future(message = "Appointment time must be in the future") // Ensures the appointment time is a future date/time.
    private LocalDateTime appointmentTime; // The exact date and time of the appointment.

    @NotNull(message = "Status cannot be null") // Ensures the appointment status is provided.
    private int status; // Status of the appointment (0 = Scheduled, 1 = Completed, 2 = Cancelled).

    private String notes; // Optional notes related to the appointment.

    // Default constructor (required by JPA and Spring Data)
    public Appointment() {
    }

    // Constructor to easily create new Appointment instances with required fields.
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // --- Getters and Setters for all attributes ---

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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // --- Helper Methods (Transient, not persisted in DB) ---

    /**
     * Calculates and returns the estimated end time of the appointment.
     * Assumes a fixed appointment duration of 1 hour.
     * @return The LocalDateTime representing the end time of the appointment.
     */
    @Transient // Marks this method's return value as not needing to be persisted in the database.
    public LocalDateTime getEndTime() {
        if (this.appointmentTime == null) {
            return null; // Or throw an exception, depending on desired error handling
        }
        return this.appointmentTime.plusHours(1); // Assuming 1 hour duration
    }

    /**
     * Extracts and returns only the date portion of the appointment time.
     * @return The LocalDate representing the date of the appointment.
     */
    @Transient // Marks this method's return value as not needing to be persisted in the database.
    public LocalDate getAppointmentDate() {
        if (this.appointmentTime == null) {
            return null;
        }
        return this.appointmentTime.toLocalDate();
    }

    /**
     * Extracts and returns only the time portion of the appointment time.
     * @return The LocalTime representing the time of the appointment.
     */
    @Transient // Marks this method's return value as not needing to be persisted in the database.
    public LocalTime getAppointmentTimeOnly() {
        if (this.appointmentTime == null) {
            return null;
        }
        return this.appointmentTime.toLocalTime();
    }

    @Override
    public String toString() {
        return "Appointment{" +
               "id=" + id +
               ", doctorId=" + (doctor != null ? doctor.getId() : "null") +
               ", patientId=" + (patient != null ? patient.getId() : "null") +
               ", appointmentTime=" + appointmentTime +
               ", status=" + status +
               '}';
    }
}
