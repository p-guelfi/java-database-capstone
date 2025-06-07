// adminDashboard.js
// This file contains the JavaScript logic for the Admin Dashboard,
// managing doctor display, search, filters, and adding new doctors.

import { openModal, closeModal } from './components/modals.js'; // For modal interactions
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js'; // Doctor API services
import { createDoctorCard } from './components/doctorCard.js'; // Component to render doctor cards
import { showAlert } from './util.js'; // Utility for alerts
import { renderDoctorCards } from './render.js'; // Utility for rendering lists of cards

// Global references to DOM elements
const contentDiv = document.getElementById("content");
const searchBar = document.getElementById("searchBar");
const timeFilter = document.getElementById("timeFilter");
const specialtyFilter = document.getElementById("specialtyFilter");
const addDocBtn = document.getElementById("addDocBtn"); // Button is rendered by header.js
const noDoctorsMessage = document.getElementById("noDoctorsMessage"); // Get the message element


// --- Event Listeners ---

// Listen for the DOM to be fully loaded before running initial logic
document.addEventListener('DOMContentLoaded', async () => {
    // Add event listener for the "Add Doctor" button (dynamically added by header.js)
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor');
        });
    }

    // Add event listeners for search and filter inputs
    if (searchBar) {
        searchBar.addEventListener("input", filterDoctorsOnChange);
    }
    if (timeFilter) {
        timeFilter.addEventListener("change", filterDoctorsOnChange);
    }
    if (specialtyFilter) {
        specialtyFilter.addEventListener("change", filterDoctorsOnChange);
    }

    // Initial load of doctor cards
    await loadDoctorCards();
    await populateFilterDropdowns(); // Populate specialty/time filters after doctors are loaded
});


// --- Functions ---

/**
 * Loads all doctors and displays them in the dashboard.
 */
async function loadDoctorCards() {
    if (!contentDiv) {
        console.error("Content div not found in adminDashboard.html");
        return;
    }

    contentDiv.innerHTML = ''; // Clear existing content

    try {
        const doctors = await getDoctors(); // Fetch all doctors
        if (doctors && doctors.length > 0) {
            renderDoctorCards(contentDiv, doctors, createDoctorCard);
            if (noDoctorsMessage) noDoctorsMessage.style.display = 'none'; // Hide "No doctors found" message
        } else {
            if (noDoctorsMessage) noDoctorsMessage.style.display = 'block'; // Show message if no doctors
            showAlert("No doctors found in the system."); // TODO: Replace with custom modal
        }
    } catch (error) {
        console.error("Error loading doctor cards:", error);
        showAlert("Failed to load doctors. Please try again later."); // TODO: Replace with custom modal
        if (noDoctorsMessage) {
            noDoctorsMessage.textContent = "Failed to load doctors. An error occurred.";
            noDoctorsMessage.style.display = 'block';
        }
    }
}

/**
 * Handles changes in the search bar and filter dropdowns,
 * then fetches and displays filtered doctors.
 */
async function filterDoctorsOnChange() {
    const name = searchBar ? searchBar.value.trim() : '';
    const time = timeFilter ? timeFilter.value : '';
    const specialty = specialtyFilter ? specialtyFilter.value : '';

    if (!contentDiv) {
        console.error("Content div not found for filtering.");
        return;
    }

    contentDiv.innerHTML = ''; // Clear existing content before showing filtered results

    try {
        const filtered = await filterDoctors(name, time, specialty);
        if (filtered && filtered.length > 0) {
            renderDoctorCards(contentDiv, filtered, createDoctorCard);
            if (noDoctorsMessage) noDoctorsMessage.style.display = 'none';
        } else {
            if (noDoctorsMessage) {
                noDoctorsMessage.textContent = "No doctors found matching your criteria.";
                noDoctorsMessage.style.display = 'block';
            }
        }
    } catch (error) {
        console.error("Error filtering doctors:", error);
        showAlert("Failed to filter doctors. An error occurred."); // TODO: Replace with custom modal
        if (noDoctorsMessage) {
            noDoctorsMessage.textContent = "Failed to filter doctors. An error occurred.";
            noDoctorsMessage.style.display = 'block';
        }
    }
}

/**
 * Populates the specialty and time filter dropdowns based on existing doctors.
 */
async function populateFilterDropdowns() {
    try {
        const doctors = await getDoctors(); // Fetch all doctors to get unique specialties and times
        const uniqueSpecialties = new Set();
        const uniqueTimes = new Set();

        doctors.forEach(doctor => {
            if (doctor.specialty) {
                uniqueSpecialties.add(doctor.specialty);
            }
            if (doctor.availableTimes && doctor.availableTimes.length > 0) {
                doctor.availableTimes.forEach(time => uniqueTimes.add(time));
            }
        });

        // Populate Specialty Filter
        if (specialtyFilter) {
            specialtyFilter.innerHTML = '<option value="">Filter by Specialty</option>'; // Reset
            Array.from(uniqueSpecialties).sort().forEach(specialty => {
                const option = document.createElement('option');
                option.value = specialty;
                option.textContent = specialty;
                specialtyFilter.appendChild(option);
            });
        }

        // Populate Time Filter
        if (timeFilter) {
            timeFilter.innerHTML = '<option value="">Filter by Availability</option>'; // Reset
            Array.from(uniqueTimes).sort().forEach(time => {
                const option = document.createElement('option');
                option.value = time;
                option.textContent = time;
                timeFilter.appendChild(option);
            });
        }
    } catch (error) {
        console.error("Error populating filter dropdowns:", error);
        // Do not show alert here, as loadDoctorCards already handles general load errors.
    }
}

/**
 * Handles the submission of the "Add Doctor" form in the modal.
 * This function is made global so it can be called directly from the modal's form submission.
 */
window.adminAddDoctor = async function() {
    const name = document.getElementById('doctorName').value;
    const specialty = document.getElementById('doctorSpecialty').value;
    const email = document.getElementById('doctorEmail').value;
    const password = document.getElementById('doctorPassword').value;
    const phone = document.getElementById('doctorPhone').value;

    const availableTimesCheckboxes = document.querySelectorAll('#addDoctorForm input[name="doctorAvailableTimes"]:checked');
    const availableTimes = Array.from(availableTimesCheckboxes).map(cb => cb.value);

    // Basic client-side validation
    if (!name || !specialty || !email || !password || !phone) {
        showAlert("Please fill in all required fields."); // TODO: Replace with custom modal
        return;
    }

    const doctorData = { name, specialty, email, password, phone, availableTimes };
    const token = localStorage.getItem("token");

    if (!token) {
        showAlert("Authentication token missing. Please log in as Admin."); // TODO: Replace with custom modal
        window.location.href = "/";
        return;
    }

    try {
        const result = await saveDoctor(doctorData, token);
        if (result.success) {
            showAlert(result.message); // TODO: Replace with custom modal
            closeModal(); // Close the modal
            await loadDoctorCards(); // Reload the doctor list to show the new doctor
        } else {
            showAlert(result.message); // TODO: Replace with custom modal
        }
    } catch (error) {
        console.error("Error adding doctor:", error);
        showAlert("An unexpected error occurred while adding the doctor."); // TODO: Replace with custom modal
    }
};

// Expose adminAddDoctor globally to be called by modals.js
// This was already handled in the setTimeout block in modals.js, but explicitly making it a window property is robust.
// window.adminAddDoctor = adminAddDoctor; // This is now done via a global function in the setTimeout of modals.js
