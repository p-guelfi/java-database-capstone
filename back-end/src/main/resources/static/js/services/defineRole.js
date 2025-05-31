// src/main/resources/static/js/services/defineRole.js

import { openModal, closeModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';

// Define login endpoints
const ADMIN_LOGIN_ENDPOINT = `${API_BASE_URL}/api/admin/login`; // Assuming /api prefix for backend
const DOCTOR_LOGIN_ENDPOINT = `${API_BASE_URL}/api/doctor/login`; // Assuming /api prefix for backend
const PATIENT_LOGIN_ENDPOINT = `${API_BASE_URL}/api/patient/login`; // Assuming /api prefix for backend

// Helper function to save role (as per lab note)
function selectRole(role) {
    localStorage.setItem('selectedRole', role);
    console.log(`Role selected: ${role}`);
}

// Setup Button Event Listeners are mostly handled by inline onclicks in HTML
// This script will define the functions called by those onclicks.
document.addEventListener('DOMContentLoaded', () => {
    // You can add additional DOM-related setup here if needed,
    // but core button listeners are wired via window.open...Modal
    // which are already made globally accessible in the inline script of defineRole.html
});


// Implement Admin Login Handler (made globally accessible via window)
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
            selectRole("admin"); // Call helper function as per lab
            localStorage.setItem('adminToken', data.token); // Store token
            alert('Admin login successful!');
            closeModal('adminLoginModal'); // Close modal on success
            window.location.href = '../templates/admin/adminDashboard.html'; // Redirect to Admin dashboard
        } else {
            const errorData = await response.json();
            alert(`Invalid credentials! ${errorData.message || ''}`);
        }
    } catch (error) {
        console.error('Error during Admin login:', error);
        alert('An unexpected error occurred during Admin login.');
    }
};

// Implement Doctor Login Handler (made globally accessible via window)
window.doctorLoginHandler = async () => {
    const emailInput = document.getElementById('doctor-email');
    const passwordInput = document.getElementById('doctor-password');

    const email = emailInput.value;
    const password = passwordInput.value;

    const doctor = { email, password };

    try {
        const response = await fetch(DOCTOR_LOGIN_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            selectRole("doctor"); // Call helper function as per lab
            localStorage.setItem('doctorToken', data.token); // Store token
            alert('Doctor login successful!');
            closeModal('doctorLoginModal'); // Close modal on success
            window.location.href = '../templates/doctor/doctorDashboard.html'; // Redirect to Doctor dashboard
        } else {
            const errorData = await response.json();
            alert(`Invalid credentials! ${errorData.message || ''}`);
        }
    } catch (error) {
        console.error('Error during Doctor login:', error);
        alert('An unexpected error occurred during Doctor login.');
    }
};

// Implement Patient Login Handler (made globally accessible via window)
// This is a placeholder; actual login logic depends on your backend patient endpoint
window.patientLoginHandler = async () => {
    const usernameInput = document.getElementById('patient-username');
    const passwordInput = document.getElementById('patient-password');

    const username = usernameInput.value;
    const password = passwordInput.value;

    const patient = { username, password }; // Adjust based on your backend DTO

    try {
        const response = await fetch(PATIENT_LOGIN_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(patient)
        });

        if (response.ok) {
            const data = await response.json();
            selectRole("patient");
            localStorage.setItem('patientToken', data.token);
            alert('Patient login successful!');
            closeModal('patientLoginModal');
            window.location.href = '../pages/patientDashboard.html'; // Assuming patient dashboard is in pages
        } else {
            const errorData = await response.json();
            alert(`Invalid credentials! ${errorData.message || ''}`);
        }
    } catch (error) {
        console.error('Error during Patient login:', error);
        alert('An unexpected error occurred during Patient login.');
    }
};
