package com.project.back_end.service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repository.mongodb.PrescriptionRepository; // Import MongoDB repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling the creation and retrieval of prescriptions.
 * This service interacts with the MongoDB repository for prescription data.
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription to the database.
     *
     * @param prescription The prescription object to be saved.
     * @return ResponseEntity with a message indicating the result of the save operation.
     * Returns 201 Created on success, 500 Internal Server Error on failure.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created
        } catch (Exception e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            response.put("message", "Failed to save prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    /**
     * Retrieves the prescription(s) associated with a specific appointment ID.
     *
     * @param appointmentId The appointment ID whose associated prescription(s) are to be retrieved.
     * @return ResponseEntity containing the prescription details or an error message.
     * Returns 200 OK with data, 404 Not Found if no prescriptions, or 500 Internal Server Error on failure.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);

            if (prescriptions.isEmpty()) {
                response.put("message", "No prescriptions found for appointment ID: " + appointmentId);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }

            response.put("prescriptions", prescriptions);
            response.put("message", "Prescription(s) retrieved successfully.");
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK
        } catch (Exception e) {
            System.err.println("Error retrieving prescription for appointment ID " + appointmentId + ": " + e.getMessage());
            response.put("message", "Failed to retrieve prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }
}
