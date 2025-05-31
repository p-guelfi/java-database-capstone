// src/main/resources/static/js/adminDashboard.js

import { renderHeader } from './components/header.js';
import { renderFooter } from './components/footer.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal, closeModal } from './components/modals.js';
import { getDoctors, saveDoctor, filterDoctors, deleteDoctor } from './services/doctorServices.js';

let allDoctors = []; // Store all fetched doctors for client-side filtering

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Render Header and Footer
    renderHeader();
    renderFooter();

    // 2. Auth Check: Ensure admin is logged in
    const adminToken = localStorage.getItem('adminToken');
    const selectedRole = localStorage.getItem('selectedRole');

    if (!adminToken || selectedRole !== 'admin') {
        alert('Unauthorized access. Please log in as an Admin.');
        window.location.href = '/index.html'; // Redirect to login page
        return; // Stop execution
    }

    // 3. Get references to DOM elements
    const doctorCardsContainer = document.getElementById('doctor-cards-container');
    const addDoctorButton = document.getElementById('add-doctor-btn');
    const addDoctorModal = document.getElementById('addDoctorModal'); // Make sure this ID is in adminDashboard.html
    const addDoctorForm = document.getElementById('add-doctor-form');
    const searchInput = document.getElementById('search-doctors');
    const specialtyFilter = document.getElementById('filter-specialty');
    // Add more filter elements as needed (e.g., time filter)

    // Helper function to render doctors
    async function renderDoctorCards(doctorsToRender) {
        doctorCardsContainer.innerHTML = ''; // Clear existing cards
        if (doctorsToRender.length === 0) {
            doctorCardsContainer.innerHTML = '<p>No doctors found.</p>';
            return;
        }
        doctorsToRender.forEach(doctor => {
            const card = createDoctorCard(doctor, selectedRole); // Pass selectedRole for button logic
            doctorCardsContainer.appendChild(card);
        });
        attachDoctorCardActionListeners(); // Attach listeners after cards are rendered
    }

    // Function to load and display doctors
    async function loadDoctors() {
        try {
            allDoctors = await getDoctors(adminToken); // Fetch all doctors
            await renderDoctorCards(allDoctors); // Display all doctors initially
        } catch (error) {
            console.error('Failed to load doctors:', error);
            alert('Failed to load doctors.');
        }
    }

    // Initial load of doctors
    await loadDoctors();

    // 4. Implement Search and Filter functionality
    function applyFilters() {
        const searchTerm = searchInput.value;
        const selectedSpecialty = specialtyFilter.value;
        const filteredDoctors = filterDoctors(allDoctors, searchTerm, selectedSpecialty);
        renderDoctorCards(filteredDoctors);
    }

    if (searchInput) {
        searchInput.addEventListener('input', applyFilters);
    }
    if (specialtyFilter) {
        specialtyFilter.addEventListener('change', applyFilters);
    }
    // Add event listeners for other filters here

    // 5. Open Modal to Add New Doctor
    if (addDoctorButton) {
        addDoctorButton.addEventListener('click', () => {
            // Clear form for new entry
            addDoctorForm.reset();
            // Set hidden input for doctor ID to empty for new doctor
            const doctorIdInput = document.getElementById('add-doctor-id'); // Make sure you have this in your form
            if (doctorIdInput) doctorIdInput.value = '';
            openModal('addDoctorModal');
        });
    }

    // 6. Handle Add/Edit Doctor Form Submission
    if (addDoctorForm) {
        addDoctorForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // Prevent default form submission

            const doctorId = document.getElementById('add-doctor-id')?.value; // Get ID for edit
            const name = document.getElementById('doctor-name').value;
            const email = document.getElementById('doctor-email').value;
            const specialty = document.getElementById('doctor-specialty').value;
            const phone = document.getElementById('doctor-phone').value;
            const bio = document.getElementById('doctor-bio').value;

            const doctorData = { name, email, specialty, phone, bio };
            if (doctorId) {
                doctorData.id = doctorId; // Add ID for update operation
            }

            try {
                const savedDoctor = await saveDoctor(doctorData, adminToken);
                if (savedDoctor) {
                    alert(`Doctor ${doctorId ? 'updated' : 'added'} successfully!`);
                    closeModal('addDoctorModal');
                    await loadDoctors(); // Reload doctors to update UI
                } else {
                    alert(`Failed to ${doctorId ? 'update' : 'add'} doctor.`);
                }
            } catch (error) {
                console.error('Error saving doctor:', error);
                alert(`An error occurred while saving the doctor: ${error.message}`);
            }
        });
    }

    // Function to attach listeners to doctor card actions (delete/edit)
    function attachDoctorCardActionListeners() {
        // Delete Doctor
        document.querySelectorAll('.delete-doctor-btn').forEach(button => {
            button.onclick = async (event) => { // Use onclick to re-attach easily
                const doctorId = event.target.dataset.id;
                if (confirm(`Are you sure you want to delete doctor with ID: ${doctorId}?`)) {
                    try {
                        const success = await deleteDoctor(doctorId, adminToken);
                        if (success) {
                            alert('Doctor deleted successfully!');
                            await loadDoctors(); // Reload doctors to update UI
                        } else {
                            alert('Failed to delete doctor.');
                        }
                    } catch (error) {
                        console.error('Error deleting doctor:', error);
                        alert(`An error occurred while deleting the doctor: ${error.message}`);
                    }
                }
            };
        });

        // Edit Doctor (populate modal with existing data)
        document.querySelectorAll('.edit-doctor-btn').forEach(button => {
            button.onclick = () => {
                const doctorId = button.dataset.id;
                const doctorToEdit = allDoctors.find(d => d.id == doctorId); // Find doctor by ID

                if (doctorToEdit) {
                    document.getElementById('add-doctor-id').value = doctorToEdit.id;
                    document.getElementById('doctor-name').value = doctorToEdit.name;
                    document.getElementById('doctor-email').value = doctorToEdit.email;
                    document.getElementById('doctor-specialty').value = doctorToEdit.specialty || '';
                    document.getElementById('doctor-phone').value = doctorToEdit.phone || '';
                    document.getElementById('doctor-bio').value = doctorToEdit.bio || '';
                    openModal('addDoctorModal'); // Reuse the add doctor modal for editing
                } else {
                    alert('Doctor not found for editing.');
                }
            };
        });
    }
});
