// index.js
// This file handles role selection on the homepage and manages the login process
// for Admin, Doctor, and Patient roles, including interaction with login modals.

import { API_BASE_URL } from '../config/config.js'; // Import API base URL
import { openModal, closeModal } from '../components/modals.js'; // Import modal control functions
import { selectRole } from '../render.js'; // Import role selection logic from render.js
import { showAlert } from '../util.js'; // Import custom alert function

// Define API endpoints for login
const ADMIN_LOGIN_API = `${API_BASE_URL}/admin/login`; // Admin login endpoint
const DOCTOR_LOGIN_API = `${API_BASE_URL}/doctor/login`; // Doctor login endpoint
const PATIENT_LOGIN_API = `${API_BASE_URL}/patient/login`; // New: Patient login endpoint

/**
 * Attaches event listeners to the role selection buttons on the homepage.
 * This function runs when the window has fully loaded.
 */
window.onload = function () {
    // On the homepage, ensure localStorage is clean to prevent stale sessions
    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        localStorage.removeItem("userId"); // Clear userId as well
    }

    // Get button elements
    const adminBtn = document.getElementById('adminBtn');
    const doctorBtn = document.getElementById('doctorBtn');
    const patientBtn = document.getElementById('patientBtn');

    // Attach event listeners to the buttons
    if (adminBtn) {
        adminBtn.addEventListener('click', () => {
            openModal('adminLogin');
        });
    }

    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => {
            openModal('doctorLogin');
        });
    }

    if (patientBtn) {
        patientBtn.addEventListener('click', () => {
            selectRole('patient'); // This function handles redirection for patient
        });
    }
};


/**
 * Handles the login attempt for an Admin user.
 * This function is made global so it can be called directly from the modal's form submission.
 */
window.adminLoginHandler = async function() {
    const usernameInput = document.getElementById('adminUsername');
    const passwordInput = document.getElementById('adminPassword');

    if (!usernameInput || !passwordInput) {
        showAlert("Login form elements not found.");
        return;
    }

    const username = usernameInput.value;
    const password = passwordInput.value;

    const credentials = { username, password };

    try {
        const response = await fetch(ADMIN_LOGIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.token); // Store the received token
            localStorage.setItem("userRole", "admin"); // Explicitly set role
            // Assuming adminId is returned as 'userId' for consistency
            if (data.userId) {
                localStorage.setItem("userId", data.userId);
            }
            closeModal(); // Close the login modal
            showAlert("Admin login successful!"); // TODO: Replace with custom modal
            // Redirect to admin dashboard via the Spring MVC controller
            window.location.href = "/adminDashboard/" + data.token;
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || "Invalid credentials!"); // TODO: Replace with custom modal
        }
    }
    catch (error) {
        console.error("Error during admin login:", error);
        showAlert("An unexpected error occurred during login. Please try again."); // TODO: Replace with custom modal
    }
};

/**
 * Handles the login attempt for a Doctor user.
 * This function is made global so it can be called directly from the modal's form submission.
 */
window.doctorLoginHandler = async function() {
    const emailInput = document.getElementById('doctorEmail');
    const passwordInput = document.getElementById('doctorPassword');

    if (!emailInput || !passwordInput) {
        showAlert("Login form elements not found.");
        return;
    }

    const email = emailInput.value;
    const password = passwordInput.value;

    const credentials = { email, password };

    try {
        const response = await fetch(DOCTOR_LOGIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.token); // Store the received token
            localStorage.setItem("userRole", "doctor"); // Explicitly set role
            // Assuming doctorId is returned as 'userId' for consistency
            if (data.userId) {
                localStorage.setItem("userId", data.userId);
            }
            closeModal(); // Close the login modal
            showAlert("Doctor login successful!"); // TODO: Replace with custom modal
            // Redirect to doctor dashboard via the Spring MVC controller
            window.location.href = "/doctorDashboard/" + data.token;
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || "Invalid credentials!"); // TODO: Replace with custom modal
        }
    } catch (error) {
        console.error("Error during doctor login:", error);
        showAlert("An unexpected error occurred during login. Please try again."); // TODO: Replace with custom modal
    }
};

/**
 * Handles the login attempt for a Patient user.
 * This function is made global so it can be called directly from the modal's form submission.
 */
window.patientLoginHandler = async function() {
    const emailInput = document.getElementById('patientLoginEmail'); // Correct ID for patient login email
    const passwordInput = document.getElementById('patientLoginPassword'); // Correct ID for patient login password

    if (!emailInput || !passwordInput) {
        showAlert("Login form elements not found.");
        return;
    }

    const email = emailInput.value;
    const password = passwordInput.value;

    const credentials = { email, password };

    try {
        const response = await fetch(PATIENT_LOGIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.token); // Store the received token
            localStorage.setItem("userRole", "loggedPatient"); // Set role to 'loggedPatient'
            // Assuming patientId is returned as 'userId' for consistency
            if (data.userId) {
                localStorage.setItem("userId", data.userId);
            }
            closeModal(); // Close the login modal
            showAlert("Patient login successful!"); // TODO: Replace with custom modal
            // Redirect to patient dashboard after successful login
            window.location.href = "/patientDashboard/" + data.token;
        } else {
            const errorData = await response.json();
            showAlert(errorData.message || "Invalid credentials!"); // TODO: Replace with custom modal
        }
    } catch (error) {
        console.error("Error during patient login:", error);
        showAlert("An unexpected error occurred during patient login. Please try again."); // TODO: Replace with custom modal
    }
};
