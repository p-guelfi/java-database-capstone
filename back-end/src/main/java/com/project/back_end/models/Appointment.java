package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull(message = "Doctor cannot be null")
    private Doctor doctor;

    @ManyToOne
    @NotNull(message = "Patient cannot be null")
    private Patient patient;

    @Future(message = "Appointment time must be in the future")
    @NotNull(message = "Appointment time is required")
    private LocalDateTime appointmentTime;

    @NotNull(message = "Status cannot be null")
    private int status; // 0 = Scheduled, 1 = Completed

    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime.plusHours(1);
    }

    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime.toLocalDate();
    }

    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime.toLocalTime();
    }

    // Getters and Setters

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
}
