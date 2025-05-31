package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repository.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // Marks this class as a Spring Service component
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    // Constructor Injection
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a prescription to the database.
     *
     * @param prescription The prescription object to be saved.
     * @return A response with a message indicating the result of the save operation.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved successfully.");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // HTTP 201 Created
        } catch (Exception e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            response.put("message", "Failed to save prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500 Internal Server Error
        }
    }

    /**
     * Retrieves the prescription(s) associated with a specific appointment ID.
     * Note: Given `findByAppointmentId` returns a List, this method returns a list of prescriptions.
     * If you expect only one prescription per appointment, you might adjust the repository method or
     * add logic to handle multiple results (e.g., return the first, or throw if > 1).
     *
     * @param appointmentId The appointment ID whose associated prescription(s) is to be retrieved.
     * @return A response containing the prescription details or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Your repository method `findByAppointmentId` returns a List<Prescription>
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);

            if (prescriptions.isEmpty()) {
                response.put("message", "No prescriptions found for appointment ID: " + appointmentId);
                response.put("prescriptions", Collections.emptyList());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // HTTP 404 Not Found
            } else {
                response.put("message", "Prescriptions retrieved successfully.");
                response.put("prescriptions", prescriptions); // Return the list of prescriptions
                return new ResponseEntity<>(response, HttpStatus.OK); // HTTP 200 OK
            }
        } catch (Exception e) {
            System.err.println("Error retrieving prescriptions for appointment ID " + appointmentId + ": " + e.getMessage());
            response.put("message", "Failed to retrieve prescriptions: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500 Internal Server Error
        }
    }
}