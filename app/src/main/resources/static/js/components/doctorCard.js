// doctorCard.js
// This component creates a dynamic, reusable card for displaying doctor information
// on the Admin and Patient dashboards.

// Import necessary service functions. These will be implemented in subsequent steps.
import { deleteDoctor } from '../services/doctorServices.js'; // For Admin: deleting doctors
import { getPatientData } from '../services/patientServices.js'; // For Logged Patient: fetching patient data
import { openModal } from './modals.js'; // Import openModal instead of showBookingOverlay

/**
 * Creates a dynamic HTML card for a doctor.
 * The card content and available actions vary based on the user's role.
 *
 * @param {Object} doctor - The doctor object containing details like id, name, specialty, email, availableTimes.
 * @returns {HTMLElement} - The created HTML div element representing the doctor card.
 */
export function createDoctorCard(doctor) {
    // Main card container
    const card = document.createElement("div");
    card.classList.add("doctor-card");
    card.dataset.doctorId = doctor.id; // Store doctor ID on the card for easy access

    const role = localStorage.getItem("userRole"); // Get current user's role from localStorage

    // Doctor information section
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;
    infoDiv.appendChild(name);

    const specialty = document.createElement("p");
    specialty.innerHTML = `<strong>Specialty:</strong> ${doctor.specialty}`;
    infoDiv.appendChild(specialty);

    const email = document.createElement("p");
    email.innerHTML = `<strong>Email:</strong> ${doctor.email}`;
    infoDiv.appendChild(email);

    // Display available times, handle empty list gracefully
    const availability = document.createElement("p");
    if (doctor.availableTimes && doctor.availableTimes.length > 0) {
        availability.innerHTML = `<strong>Available Times:</strong> ${doctor.availableTimes.join(", ")}`;
    } else {
        availability.innerHTML = `<strong>Available Times:</strong> Not specified`;
    }
    infoDiv.appendChild(availability);

    // Button container
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // Conditionally add buttons based on the user's role
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("delete-btn"); // Add a class for specific styling
        removeBtn.addEventListener("click", async () => {
            // Confirm deletion with the user
            if (confirm(`Are you sure you want to delete doctor ${doctor.name}?`)) { // TODO: Replace with custom modal
                const token = localStorage.getItem("token"); // Get authentication token
                if (token) {
                    try {
                        // Call the deleteDoctor service function
                        const success = await deleteDoctor(doctor.id, token);
                        if (success) {
                            card.remove(); // Remove the card from the DOM on successful deletion
                            alert(`${doctor.name} deleted successfully.`); // TODO: Replace with custom modal
                            // You might want to re-render the list or update a counter here
                        } else {
                            alert(`Failed to delete doctor ${doctor.name}.`); // TODO: Replace with custom modal
                        }
                    } catch (error) {
                        console.error("Error deleting doctor:", error);
                        alert("An error occurred while deleting the doctor."); // TODO: Replace with custom modal
                    }
                } else {
                    alert("Authentication token missing. Please log in."); // TODO: Replace with custom modal
                    window.location.href = "/"; // Redirect to login
                }
            }
        });
        actionsDiv.appendChild(removeBtn);
    } else if (role === "patient") { // Unlogged patient
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("button"); // Use a general button style
        bookNow.addEventListener("click", () => {
            alert("Please log in or sign up to book an appointment."); // TODO: Replace with custom modal
            // Optionally, open the login/signup modal directly here.
        });
        actionsDiv.appendChild(bookNow);
    } else if (role === "loggedPatient") { // Logged-in patient
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("button");
        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            if (token) {
                try {
                    const patientData = await getPatientData(token); // Fetch patient's own data
                    if (patientData) {
                        // Call openModal with 'bookingOverlay' type and pass necessary data
                        openModal('bookingOverlay', { doctor: doctor, patient: patientData });
                    } else {
                        alert("Could not retrieve patient data. Please try logging in again."); // TODO: Replace with custom modal
                    }
                } catch (error) {
                    console.error("Error fetching patient data for booking:", error);
                    alert("An error occurred while preparing for booking."); // TODO: Replace with custom modal
                }
            } else {
                alert("Authentication token missing. Please log in."); // TODO: Replace with custom modal
                window.location.href = "/"; // Redirect to login
            }
        });
        actionsDiv.appendChild(bookNow);
    }

    // Append info and actions to the main card
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card; // Return the fully constructed doctor card
}
