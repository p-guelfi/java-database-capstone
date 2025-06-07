// patientDashboard.js
// This script handles the dynamic content and interactions for the patient dashboard.

import { getDoctors, filterDoctors } from './services/doctorServices.js'; // Import doctor services for fetching and filtering
import { renderDoctorCards } from './components/doctorCard.js'; // Import function to render doctor cards
import { showAlert } from './util.js'; // Import custom alert utility

let allDoctors = []; // Store all fetched doctors for local filtering/display

/**
 * Initializes the patient dashboard by fetching and rendering doctors,
 * and setting up event listeners for search and filters.
 */
async function initPatientDashboard() {
    // Get references to the DOM elements
    const searchBar = document.getElementById('searchBar');
    const timeFilter = document.getElementById('timeFilter');
    const specialtyFilter = document.getElementById('specialtyFilter');
    const doctorCardsContainer = document.getElementById('content');
    const noDoctorsMessage = document.getElementById('noDoctorsMessage');

    /**
     * Fetches doctors based on current filter values and renders them.
     * @param {string} nameFilter - The name to filter by.
     * @param {string} timeFilterValue - The availability time to filter by.
     * @param {string} specialtyFilterValue - The specialty to filter by.
     */
    async function fetchAndRenderFilteredDoctors(nameFilter, timeFilterValue, specialtyFilterValue) {
        try {
            // Call the filterDoctors service with current criteria
            const filteredDoctors = await filterDoctors(nameFilter, timeFilterValue, specialtyFilterValue);

            if (filteredDoctors && filteredDoctors.length > 0) {
                renderDoctorCards(filteredDoctors, doctorCardsContainer); // Render the filtered doctors
                noDoctorsMessage.style.display = 'none'; // Hide "No doctors found" message
            } else {
                doctorCardsContainer.innerHTML = ''; // Clear existing cards
                noDoctorsMessage.style.display = 'block'; // Show "No doctors found" message
            }
        } catch (error) {
            console.error("Error fetching and rendering filtered doctors:", error);
            showAlert("Failed to load doctors. Please try again later.");
            doctorCardsContainer.innerHTML = '';
            noDoctorsMessage.style.display = 'block';
        }
    }

    // Initial load of all doctors
    await fetchAndRenderFilteredDoctors('null', 'null', 'null'); // Load all doctors initially

    // Event listener for search bar input
    if (searchBar) {
        searchBar.addEventListener('input', async (event) => {
            const name = event.target.value.trim();
            const time = timeFilter ? timeFilter.value : 'null'; // Get current time filter value
            const specialty = specialtyFilter ? specialtyFilter.value : 'null'; // Get current specialty filter value
            await fetchAndRenderFilteredDoctors(name || 'null', time, specialty); // Pass 'null' if name is empty
        });
    }

    // Event listeners for filter dropdowns (Time and Specialty)
    if (timeFilter) {
        timeFilter.addEventListener('change', async () => {
            const name = searchBar ? searchBar.value.trim() : 'null';
            const time = timeFilter.value;
            const specialty = specialtyFilter ? specialtyFilter.value : 'null';
            await fetchAndRenderFilteredDoctors(name || 'null', time, specialty);
        });
    }

    if (specialtyFilter) {
        specialtyFilter.addEventListener('change', async () => {
            const name = searchBar ? searchBar.value.trim() : 'null';
            const time = timeFilter ? timeFilter.value : 'null';
            const specialty = specialtyFilter.value;
            await fetchAndRenderFilteredDoctors(name || 'null', time, specialty);
        });
    }

    // TODO: Populate the Specialty Filter dropdown dynamically
    // This would involve another API call to get all available specialties
    // and then dynamically adding <option> tags to the specialtyFilter select element.
    // Example (pseudo-code):
    // const specialties = await getAvailableSpecialties(); // A new function in doctorServices
    // specialties.forEach(s => {
    //     const option = document.createElement('option');
    //     option.value = s;
    //     option.textContent = s;
    //     specialtyFilter.appendChild(option);
    // });
}

// Initialize the dashboard when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', initPatientDashboard);
