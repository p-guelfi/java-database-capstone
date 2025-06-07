// header.js
// This file defines and renders the dynamic header component that changes
// based on the user's role and login status.

// Import functions from other service files. These will be implemented later.
// Note: These imports assume that these modules will eventually export these functions.
import { getPatientData } from '../services/patientServices.js'; // Assuming this service exists
import { deleteDoctor } from '../services/doctorServices.js'; // Assuming this service exists
import { openModal } from './modals.js'; // Assuming openModal is exported from modals.js

/**
 * Global function to handle user logout.
 * Clears user role and token from localStorage and redirects to the homepage.
 */
window.logout = function() {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    alert("You have been logged out successfully."); // Use a custom modal instead of alert in production
    window.location.href = "/"; // Redirect to homepage
};

/**
 * Global function specifically for patient logout.
 * Clears the token and sets the role back to 'patient' (not loggedPatient)
 * to allow them to see the login/signup options on their dashboard.
 */
window.logoutPatient = function() {
    localStorage.removeItem("token");
    // Ensure the role reverts to 'patient' for access to login/signup on their dashboard
    localStorage.setItem("userRole", "patient");
    alert("You have been logged out from your patient account."); // Use a custom modal instead of alert in production
    // Redirect to the patient dashboard page (e.g., patientDashboard.html if it exists)
    // For now, redirect to '/' which will then re-evaluate the role
    window.location.href = "/";
};

/**
 * Renders the header content dynamically based on the current user's role and login state.
 * It injects appropriate navigation links, role selectors, and logout buttons.
 */
export function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) {
        console.error("Header div with ID 'header' not found.");
        return;
    }

    let headerContent = ''; // Initialize header content string

    // On the homepage, clear role and token to ensure a fresh start
    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // Handle invalid sessions: if a role is set but no token, clear and redirect to login
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        // localStorage.removeItem("token"); // Already removed above for specific case
        alert("Session expired or invalid login. Please log in again."); // TODO: Replace with custom modal
        window.location.href = "/"; // Redirect to homepage
        return; // Stop further header rendering for this invalid state
    }

    // Build header content based on the role
    headerContent = `
        <div class="logo">
            <img src="./assets/images/logo/logo.png" alt="Smart Clinic Logo">
            <h1>Smart Clinic</h1>
        </div>
        <nav>
            <ul>
    `;

    if (role === "admin") {
        headerContent += `
                <li><button id="addDocBtn" class="adminBtn">Add Doctor</button></li>
                <li><button id="logoutBtn" onclick="logout()">Logout</button></li>
        `;
    } else if (role === "doctor") {
        headerContent += `
                <li><a href="/doctorDashboard.html">Home</a></li>
                <li><button id="logoutBtn" onclick="logout()">Logout</button></li>
        `;
    } else if (role === "patient") { // Unlogged patient
        headerContent += `
                <li><button id="loginBtn">Login</button></li>
                <li><button id="signUpBtn">Sign Up</button></li>
        `;
    } else if (role === "loggedPatient") { // Logged-in patient
        headerContent += `
                <li><a href="/patientDashboard.html">Home</a></li>
                <li><a href="/patientAppointments.html">Appointments</a></li>
                <li><button id="logoutPatientBtn" onclick="logoutPatient()">Logout</button></li>
        `;
    } else { // Default for index.html or unhandled roles (should be covered by index.html direct buttons)
        headerContent += `
                <!-- Role selection buttons are handled directly in index.html, not in this dynamic header nav -->
        `;
    }

    headerContent += `
            </ul>
        </nav>
    `;

    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
}

/**
 * Attaches event listeners to dynamically created buttons in the header.
 * This function needs to be called after the header HTML is injected into the DOM.
 */
function attachHeaderButtonListeners() {
    const role = localStorage.getItem("userRole");

    // Admin-specific button
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor'); // Call the openModal function from modals.js
        });
    }

    // Logout button (common for admin/doctor)
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener('click', window.logout); // Use the global logout function
    }

    // Logout for loggedPatient
    const logoutPatientBtn = document.getElementById("logoutPatientBtn");
    if (logoutPatientBtn) {
        logoutPatientBtn.addEventListener('click', window.logoutPatient); // Use the global logoutPatient function
    }

    // Patient (unlogged) specific buttons
    const loginBtn = document.getElementById("loginBtn");
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            openModal('patientLogin'); // Example: open patient login modal
        });
    }

    const signUpBtn = document.getElementById("signUpBtn");
    if (signUpBtn) {
        signUpBtn.addEventListener('click', () => {
            openModal('patientSignUp'); // Example: open patient signup modal
        });
    }
}

// Render the header when this script loads
document.addEventListener('DOMContentLoaded', renderHeader);
