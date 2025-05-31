# Smart Clinic System – Schema Design

## MySQL Database Design

### Table: patients
- `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
- `name`: VARCHAR(100), NOT NULL  
- `email`: VARCHAR(100), UNIQUE, NOT NULL  
- `phone`: VARCHAR(20), NOT NULL  
- `date_of_birth`: DATE  
- `created_at`: DATETIME, DEFAULT CURRENT_TIMESTAMP  

### Table: doctors
- `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
- `name`: VARCHAR(100), NOT NULL  
- `specialization`: VARCHAR(100)  
- `email`: VARCHAR(100), UNIQUE, NOT NULL  
- `phone`: VARCHAR(20)  
- `created_at`: DATETIME, DEFAULT CURRENT_TIMESTAMP  

### Table: appointments
- `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
- `doctor_id`: INT, FOREIGN KEY → doctors(id), NOT NULL  
- `patient_id`: INT, FOREIGN KEY → patients(id), NOT NULL  
- `appointment_time`: DATETIME, NOT NULL  
- `status`: INT DEFAULT 0  -- 0 = Scheduled, 1 = Completed, 2 = Cancelled  
- `notes`: TEXT  

### Table: admin
- `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
- `username`: VARCHAR(50), UNIQUE, NOT NULL  
- `password`: VARCHAR(255), NOT NULL  
- `email`: VARCHAR(100), UNIQUE  
- `role`: VARCHAR(50), DEFAULT 'admin'  

> Notes:
> - Appointments have foreign keys to both doctors and patients.
> - If a patient or doctor is deleted, cascading delete could be considered for appointments.
> - Emails are set as UNIQUE to prevent duplicate registrations.
> - Dates and timestamps help track record creation.

---

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('665f12abc123')",
  "patientId": 1,
  "appointmentId": 12,
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "instructions": "Take one capsule every 8 hours"
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "instructions": "Take one tablet after meals"
    }
  ],
  "doctorNotes": "Patient reported mild fever and sore throat.",
  "issuedAt": "2025-05-31T10:00:00Z",
  "pharmacy": {
    "name": "CityMed Pharmacy",
    "location": "Downtown"
  }
}

> Notes:
> - medications is an array to support multiple drugs in one prescription.

> - notes is free-form text, which is ideal for MongoDB’s flexible structure.

> - pharmacy is an embedded document to keep related info together and allow fast access.

> - We store only patientId and appointmentId to avoid duplicating full objects and to keep documents lightweight.

> - This structure allows future additions (e.g., tags, timestamps) without major schema changes