package com.project.back_end.repository.mongodb;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the Prescription entity, specifically for MongoDB.
 * This interface extends MongoRepository to inherit standard CRUD operations
 * for Prescription documents, with String as the type of the primary key (_id).
 * It also defines custom query methods for finding prescriptions.
 */
@Repository // Marks this interface as a Spring Data MongoDB repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Finds all prescriptions associated with a specific appointment ID.
     * Spring Data MongoDB automatically generates the query for this method based on its name.
     *
     * @param appointmentId The ID of the appointment to find prescriptions for.
     * @return A list of prescriptions matching the given appointment ID.
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
