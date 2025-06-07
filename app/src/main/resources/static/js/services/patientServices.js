// patientServices.js
// This module centralizes all API communication related to patient data,
// including sign-up, login, and appointment management, improving reusability and maintainability.

import { API_BASE_URL } from '../config/config.js'; // Import the base API URL
import { showAlert } from '../util.js'; // Import custom alert function

const PATIENT_API = `${API_BASE_URL}/patient`; // Base endpoint for patient-related operations
const APPOINTMENT_API = `${API_BASE_URL}/appointment`; // Base endpoint for appointment-related operations

/**
 * Handles patient signup by sending patient details to the backend.
 * @param {Object} data - Patient details (e.g., name, email, password, phone, address).
 * @returns {Object} - An object indicating success status and a message.
 */
export async function patientSignup(data) {
    try {
        const response = await fetch(`${PATIENT_API}/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            const result = await response.json();
            return { success: true, message: result.message || "Signup successful!" };
        } else {
            const errorData = await response.json();
            console.error("Signup failed:", errorData.message);
            return { success: false, message: errorData.message || "Signup failed. Please try again." };
        }
    } catch (error) {
        console.error("Network error during patient signup:", error);
        return { success: false, message: "Network error during signup. Please check your connection." };
    }
}

/**
 * Handles patient login by sending credentials to the backend.
 * @param {Object} data - Patient login credentials (email, password).
 * @returns {Response} - The full fetch API response object. Frontend needs to check .ok and extract token.
 */
export async function patientLogin(data) {
    try {
        const response = await fetch(`${PATIENT_API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response; // Return full response for detailed handling in UI
    } catch (error) {
        console.error("Network error during patient login:", error);
        showAlert("Network error during login. Please try again."); // TODO: Replace with custom modal
        // Throwing error so caller can catch it and handle appropriately
        throw error;
    }
}

/**
 * Fetches data for the currently logged-in patient.
 * @param {string} token - Authentication token for the patient.
 * @returns {Object|null} - The patient object if successful, null otherwise.
 */
export async function getPatientData(token) {
    try {
        const response = await fetch(`${PATIENT_API}/profile`, { // Assuming an endpoint like /patient/profile
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const patient = await response.json();
            return patient;
        } else {
            const errorData = await response.json();
            console.error("Failed to fetch patient data:", errorData.message);
            showAlert(`Error fetching patient data: ${errorData.message}`); // TODO: Replace with custom modal
            return null;
        }
    } catch (error) {
        console.error("Network error while fetching patient data:", error);
        showAlert("Network error while fetching patient data. Please try again."); // TODO: Replace with custom modal
        return null;
    }
}

/**
 * Fetches appointments for a specific patient, usable by both patient and doctor dashboards.
 * @param {Long} id - The ID of the patient whose appointments are to be fetched.
 * @param {string} token - The authentication token (patient's or doctor's).
 * @param {string} userRole - The role of the user making the request ('patient' or 'doctor').
 * @returns {Array<Object>|null} - An array of appointment objects, or null if an error occurs.
 */
export async function getPatientAppointments(id, token, userRole) {
    let url = '';
    // Assuming different endpoints or query parameters based on userRole
    if (userRole === 'patient') {
        url = `${PATIENT_API}/${id}/appointments`; // Patient viewing their own appointments
    } else if (userRole === 'doctor') {
        url = `${APPOINTMENT_API}/doctor-appointments?patientId=${id}`; // Doctor viewing patient's appointments linked to them
        // This assumes the backend can filter appointments for a specific patient ID AND the logged-in doctor
    } else {
        console.error("Invalid user role for fetching appointments.");
        return null;
    }

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const appointments = await response.json();
            return appointments;
        } else {
            const errorData = await response.json();
            console.error(`Failed to fetch appointments for patient ID ${id}:`, errorData.message);
            showAlert(`Error fetching appointments: ${errorData.message}`); // TODO: Replace with custom modal
            return null;
        }
    } catch (error) {
        console.error(`Network error while fetching appointments for patient ID ${id}:`, error);
        showAlert("Network error during appointment fetch. Please try again."); // TODO: Replace with custom modal
        return null;
    }
}

/**
 * Filters appointments based on a condition (e.g., status) and patient name.
 * Assumes a backend endpoint that can handle these filters.
 * @param {string} condition - A condition for filtering appointments (e.g., 'scheduled', 'completed', 'cancelled').
 * @param {string} name - Patient's name for filtering (optional).
 * @param {string} token - Authentication token.
 * @returns {Array<Object>|null} - Filtered array of appointment objects, or null if error.
 */
export async function filterAppointments(condition, name, token) {
    const params = new URLSearchParams();
    if (condition) params.append('status', condition); // Assuming 'status' is the backend param
    if (name) params.append('patientName', name); // Assuming 'patientName' is the backend param

    const queryString = params.toString();
    const url = queryString ? `${APPOINTMENT_API}/filter?${queryString}` : APPOINTMENT_API; // Adjust endpoint as needed

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const filteredAppointments = await response.json();
            return filteredAppointments;
        } else {
            const errorData = await response.json();
            console.error("Failed to filter appointments:", errorData.message);
            showAlert(`Error filtering appointments: ${errorData.message}`); // TODO: Replace with custom modal
            return [];
        }
    } catch (error) {
        console.error("Network error while filtering appointments:", error);
        showAlert("Network error during appointment filtering. Please try again."); // TODO: Replace with custom modal
        return [];
    }
}
