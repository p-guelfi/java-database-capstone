package com.project.back_end.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object (DTO) for Appointment data.
 * This class is used to simplify and structure the appointment information
 * exchanged between the backend and frontend, decoupling it from the
 * internal database model (Appointment.java, Doctor.java, Patient.java).
 */
public class AppointmentDTO {

    private Long id; // Unique identifier for the appointment
    private Long doctorId; // ID of the doctor assigned to the appointment
    private String doctorName; // Full name of the doctor
    private Long patientId; // ID of the patient
    private String patientName; // Full name of the patient
    private String patientEmail; // Email address of the patient
    private String patientPhone; // Contact number of the patient
    private String patientAddress; // Residential address of the patient
    private LocalDateTime appointmentTime; // Full date and time of the appointment
    private int status; // Appointment status (e.g., scheduled, completed)
    private String notes; // Added: Optional notes related to the appointment
    private LocalDate appointmentDate; // Extracted date from appointmentTime
    private LocalTime appointmentTimeOnly; // Extracted time from appointmentTime
    private LocalDateTime endTime; // Calculated as appointmentTime + 1 hour (for a fixed 1-hour slot)

    /**
     * Constructor to initialize core fields and automatically compute derived time-based fields.
     *
     * @param id The unique identifier for the appointment.
     * @param doctorId The ID of the doctor.
     * @param doctorName The full name of the doctor.
     * @param patientId The ID of the patient.
     * @param patientName The full name of the patient.
     * @param patientEmail The email address of the patient.
     * @param patientPhone The phone number of the patient.
     * @param patientAddress The address of the patient.
     * @param appointmentTime The full date and time of the appointment.
     * @param status The status of the appointment.
     * @param notes The optional notes related to the appointment.
     */
    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, String patientName,
                          String patientEmail, String patientPhone, String patientAddress,
                          LocalDateTime appointmentTime, int status, String notes) { // Notes parameter added
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes; // Assign notes

        // Automatically compute derived fields
        if (this.appointmentTime != null) {
            this.appointmentDate = this.appointmentTime.toLocalDate();
            this.appointmentTimeOnly = this.appointmentTime.toLocalTime();
            this.endTime = this.appointmentTime.plusHours(1); // Assuming 1-hour duration
        }
    }

    // --- Getter Methods ---

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public String getNotes() { // Added getter for notes
        return notes;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Default constructor (often needed for deserialization if you add setters or use frameworks)
    public AppointmentDTO() {
    }

    // Optional: Add setters if this DTO will also be used for incoming requests (e.g., booking/updating appointments)
    // For now, based on "getter methods to allow serialization of the DTO in API responses",
    // only getters are primarily focused on here.
}
