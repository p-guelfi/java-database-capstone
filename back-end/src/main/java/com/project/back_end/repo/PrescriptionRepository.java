package com.project.back_end.repository; // Recommended: Create a 'repository' sub-package

import com.project.back_end.models.Prescription; // Correct import for your Prescription model
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Prescription documents in MongoDB.
 * Extends MongoRepository to inherit basic CRUD operations for MongoDB.
 */
@Repository // Marks this interface as a Spring Data MongoDB repository component
public interface PrescriptionRepository extends MongoRepository<Prescription, String> { // Prescription model, String ID type

    /**
     * Finds a list of Prescription documents associated with a specific appointment ID.
     * Spring Data MongoDB automatically generates this query based on the method name.
     *
     * @param appointmentId The ID of the appointment to search for.
     * @return A list of Prescription documents associated with the given appointment ID.
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}