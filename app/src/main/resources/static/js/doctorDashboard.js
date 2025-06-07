// doctorDashboard.js
// This file contains the JavaScript logic for the Doctor Dashboard,
// managing appointment display, search, and date filtering.

import { getAllAppointments, getUpcomingAppointments as fetchUpcomingAppointments } from './services/appointmentRecordService.js'; // Service to fetch appointments
import { createPatientRow } from './components/patientRows.js'; // Component to render patient rows in table
import { showAlert, formatDate } from './util.js'; // Utility for alerts and date formatting

// --- Global Variables ---
const patientTableBody = document.getElementById('patientTableBody');
const searchBar = document.getElementById('searchBar');
const todayAppointmentsBtn = document.getElementById('todayAppointmentsBtn');
const dateFilter = document.getElementById('dateFilter');

let selectedDate = null; // Initialize to null for "all upcoming" on initial load
let patientName = ''; // For search filtering
const token = localStorage.getItem("token"); // Authentication token for the doctor
const userRole = localStorage.getItem("userRole"); // Should be 'doctor'

// --- Event Listeners ---
document.addEventListener('DOMContentLoaded', async () => {
    // Initial setup of the date filter input
    // Do not set dateFilter.value if initially showing all upcoming
    if (dateFilter) {
        dateFilter.value = ''; // Clear date picker for "all upcoming" view
    }

    // Load appointments on page load - initially show all upcoming
    await loadAppointments(true); // Pass true to indicate initial load for upcoming

    // Event listener for search bar input
    if (searchBar) {
        searchBar.addEventListener('input', (event) => {
            patientName = event.target.value.trim();
            // If search bar is empty, consider patientName as null for broader search.
            // Backend should handle empty string/null for 'name' parameter to mean 'no filter'.
            loadAppointments(selectedDate === null); // If selectedDate is null, implies upcoming filter is active
        });
    }

    // Event listener for "Today's Appointments" button
    if (todayAppointmentsBtn) {
        todayAppointmentsBtn.addEventListener('click', () => {
            selectedDate = formatDate(new Date()); // Reset to today
            if (dateFilter) {
                dateFilter.value = selectedDate; // Update date picker UI
            }
            loadAppointments(false); // Not an "upcoming" load, specific date
        });
    }

    // Event listener for date filter input
    if (dateFilter) {
        dateFilter.addEventListener('change', (event) => {
            selectedDate = event.target.value; // Update selected date
            if (selectedDate === '') {
                // If date filter is cleared, load all upcoming appointments
                loadAppointments(true);
            } else {
                // Otherwise, load appointments for the selected specific date
                loadAppointments(false);
            }
        });
    }
});

// --- Functions ---

/**
 * Loads and displays appointments for the logged-in doctor based on filters.
 *
 * @param {boolean} [loadAllUpcoming=false] - If true, fetches all upcoming appointments.
 * If false, uses `selectedDate` and `patientName` for filtering a specific date.
 */
async function loadAppointments(loadAllUpcoming = false) {
    if (!patientTableBody) {
        console.error("Patient table body not found.");
        return;
    }
    if (!token || userRole !== 'doctor') {
        showAlert("Unauthorized access. Please log in as a doctor."); // TODO: Replace with custom modal
        window.location.href = "/"; // Redirect to homepage
        return;
    }

    patientTableBody.innerHTML = ''; // Clear existing content before loading new data

    try {
        let appointments;
        if (loadAllUpcoming) {
            // Call the new service method for all upcoming appointments
            appointments = await fetchUpcomingAppointments(patientName, token);
        } else {
            // Call the existing service method for a specific date
            // Ensure selectedDate is valid for specific date lookup
            if (!selectedDate) {
                 showAlert("Please select a date or clear the filter to view all upcoming appointments.");
                 const errorRow = document.createElement('tr');
                 errorRow.innerHTML = `<td colspan="7" class="no-records-message">Please select a date.</td>`;
                 patientTableBody.appendChild(errorRow);
                 return;
            }
            appointments = await getAllAppointments(selectedDate, patientName, token);
        }


        if (appointments && appointments.length > 0) {
            appointments.forEach(appointment => {
                const row = createPatientRow(appointment);
                patientTableBody.appendChild(row);
            });
        } else {
            // If no appointments are found, dynamically create and append the "No appointments found" row.
            const noAppointmentsRow = document.createElement('tr');
            noAppointmentsRow.id = 'noAppointmentsMessage';
            noAppointmentsRow.innerHTML = `<td colspan="7" class="no-records-message">No appointments found.</td>`;
            patientTableBody.appendChild(noAppointmentsRow);
        }
    } catch (error) {
        console.error("Error loading appointments:", error);
        showAlert("Failed to load appointments. Please try again later."); // TODO: Replace with custom modal
        // In case of an error, display an error message row.
        const errorRow = document.createElement('tr');
        errorRow.innerHTML = `<td colspan="7" class="no-records-message">Failed to load appointments. An error occurred.</td>`;
        patientTableBody.appendChild(errorRow);
    }
}
