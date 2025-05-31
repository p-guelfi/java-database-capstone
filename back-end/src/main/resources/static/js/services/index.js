// src/main/resources/static/js/services/index.js

import { openModal, closeModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';

// Define login endpoints
const ADMIN_LOGIN_ENDPOINT = `${API_BASE_URL}/admin/login`;
const DOCTOR_LOGIN_ENDPOINT = `${API_BASE_URL}/doctor/login`;

// Helper function to save role (as per lab note) - can be expanded
function selectRole(role) {
    localStorage.setItem('selectedRole', role);
    console.log(`Role selected: ${role}`);
}

// Setup Button Event Listeners
window.onload = () => {
    const adminButton = document.getElementById('admin-button');
    const doctorButton = document.getElementById('doctor-button');
    const patientButton = document.getElementById('patient-button'); // Assuming a patient button exists

    if (adminButton) {
        adminButton.addEventListener('click', () => {
            selectRole("admin"); // Save selected role
            openModal('adminLoginModal'); // Open Admin login modal
        });
    }

    if (doctorButton) {
        doctorButton.addEventListener('click', () => {
            selectRole("doctor"); // Save selected role
            openModal('doctorLoginModal'); // Open Doctor login modal
        });
    }

    if (patientButton) {
        patientButton.addEventListener('click', () => {
            selectRole("patient"); // Save selected role
            // For patient, you might open a modal, or redirect directly if no login needed
            // If patient also needs login, add patientLoginModal and handler similar to admin/doctor
            // For now, if no patient login modal, just store role and maybe redirect
            console.log("Patient role selected. Implement patient login/redirect logic.");
            // Example: window.location.href = 'patientDashboard.html';
        });
    }
};

// Implement Admin Login Handler (made global for onclick in HTML)
window.adminLoginHandler = async () => {
    const usernameInput = document.getElementById('admin-username');
    const passwordInput = document.getElementById('admin-password');

    const username = usernameInput.value;
    const password = passwordInput.value;

    const admin = { username, password };

    try {
        const response = await fetch(ADMIN_LOGIN_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(admin)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('adminToken', data.token); // Store token
            // No need to call selectRole here again if it was set on button click
            alert('Admin login successful!');
            closeModal('adminLoginModal'); // Close modal on success
            window.location.href = 'templates/admin/adminDashboard.html'; // Redirect to Admin dashboard
        } else {
            alert('Invalid credentials!');
        }
    } catch (error) {
        console.error('Error during Admin login:', error);
        alert('An unexpected error occurred during login.');
    }
};

// Implement Doctor Login Handler (made global for onclick in HTML)
window.doctorLoginHandler = async () => {
    const emailInput = document.getElementById('doctor-email');
    const passwordInput = document.getElementById('doctor-password');

    const email = emailInput.value;
    const password = passwordInput.value;

    const doctor = { email, password }; // Adjust this object based on your backend Doctor login DTO

    try {
        const response = await fetch(DOCTOR_LOGIN_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('doctorToken', data.token); // Store token
            // No need to call selectRole here again if it was set on button click
            alert('Doctor login successful!');
            closeModal('doctorLoginModal'); // Close modal on success
            window.location.href = 'templates/doctor/doctorDashboard.html'; // Redirect to Doctor dashboard
        } else {
            alert('Invalid credentials!');
        }
    } catch (error) {
        console.error('Error during Doctor login:', error);
        alert('An unexpected error occurred during login.');
    }
};
