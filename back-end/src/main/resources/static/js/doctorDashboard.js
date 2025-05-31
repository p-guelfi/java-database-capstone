// src/main/resources/static/js/doctorDashboard.js

import { renderHeader } from './components/header.js';
import { renderFooter } from './components/footer.js';
import { createPatientRow } from './components/patientRows.js'; // Import the new component
import { getPatientsForToday, getPatientsByDate, savePrescription } from './services/patientServices.js'; // Import patient services
import { closeModal } from './components/modals.js'; // Ensure closeModal is available

// Get DOM elements
const patientTableBody = document.getElementById('patientTableBody');
const searchBar = document.getElementById('searchBar');
const todayAppointmentsBtn = document.getElementById('today-appointments-btn');
const appointmentDateInput = document.getElementById('appointmentDate');
const prescriptionForm = document.getElementById('prescriptionForm');
const prescriptionTextarea = document.getElementById('prescriptionText');
const prescriptionPatientIdInput = document.getElementById('prescriptionPatientId');

// --- Loading Patient Data ---
async function loadPatientsForToday() {
    renderPatients([]); // Clear table and show loading message
    const { success, data, message } = await getPatientsForToday();
    if (success) {
        renderPatients(data);
    } else {
        console.error("Error loading today's patients:", message);
        renderPatients([]); // Show no records
    }
}

async function loadPatientsByDate(date) {
    renderPatients([]); // Clear table and show loading message
    const { success, data, message } = await getPatientsByDate(date);
    if (success) {
        renderPatients(data);
    } else {
        console.error(`Error loading patients for ${date}:`, message);
        renderPatients([]); // Show no records
    }
}

// --- Rendering Patient List ---
function renderPatients(patients) {
    patientTableBody.innerHTML = ''; // Clear existing rows

    if (!patients || patients.length === 0) {
        const noRecordsRow = document.createElement('tr');
        noRecordsRow.innerHTML = `<td colspan="5" class="no-records">No patients found for the selected date.</td>`;
        patientTableBody.appendChild(noRecordsRow);
        return;
    }

    patients.forEach(patient => {
        const row = createPatientRow(patient);
        patientTableBody.appendChild(row);
    });
}

// --- Search Functionality ---
function filterDisplayedRows() {
    const searchValue = searchBar.value.toLowerCase();
    const rows = patientTableBody.querySelectorAll('tr:not(.no-records)');

    rows.forEach(row => {
        const textContent = row.textContent.toLowerCase();
        if (textContent.includes(searchValue)) {
            row.style.display = ''; // Show row
        } else {
            row.style.display = 'none'; // Hide row
        }
    });

    // Check if all rows are hidden after filtering
    const visibleRows = Array.from(rows).filter(row => row.style.display !== 'none');
    if (visibleRows.length === 0 && patientTableBody.querySelector('.no-records') === null) {
        const noMatchRow = document.createElement('tr');
        noMatchRow.innerHTML = `<td colspan="5" class="no-records">No matching patients found.</td>`;
        patientTableBody.appendChild(noMatchRow);
    } else if (visibleRows.length > 0) {
        const noMatchRow = patientTableBody.querySelector('.no-records:last-child');
        if (noMatchRow && noMatchRow.textContent.includes('No matching')) {
            noMatchRow.remove(); // Remove temporary "No matching" message
        }
    }
}

// --- Event Handling ---
document.addEventListener('DOMContentLoaded', () => {
    renderHeader(); // Render header component
    renderFooter(); // Render footer component

    // Set today's date as default in the date input
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // Month is 0-indexed
    const day = String(today.getDate()).padStart(2, '0');
    appointmentDateInput.value = `<span class="math-inline">\{year\}\-</span>{month}-${day}`;

    loadPatientsForToday(); // Initial load for today's appointments

    // Event listeners
    todayAppointmentsBtn.addEventListener('click', loadPatientsForToday);
    appointmentDateInput.addEventListener('change', (event) => {
        loadPatientsByDate(event.target.value);
    });
    searchBar.addEventListener('input', filterDisplayedRows);

    // Prescription form submission
    prescriptionForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const patientId = prescriptionPatientIdInput.value;
        const prescriptionText = prescriptionTextarea.value;

        if (patientId && prescriptionText) {
            const { success, message } = await savePrescription(patientId, prescriptionText);
            if (success) {
                alert('Prescription saved successfully!');
                closeModal('prescriptionModal'); // Close modal
                // Optionally, re-load patients or update the table if needed
                // loadPatientsForToday();
            } else {
                alert(`Failed to save prescription: ${message}`);
            }
        } else {
            alert('Please enter prescription details.');
        }
    });
});

// Make closeModal available globally for inline onclick attributes in HTML
window.closeModal = closeModal;
