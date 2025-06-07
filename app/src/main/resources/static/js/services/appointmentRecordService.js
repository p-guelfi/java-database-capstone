// appointmentRecordService.js
// This module handles all API interactions related to appointment records,
// specifically fetching appointments for a doctor.

// --- External Dependencies (You need to provide these) ---
// 1. API_BASE_URL: A constant string representing the base URL of your backend API (e.g., 'http://localhost:8080/api').
//    This is imported from 'config/config.js'.
// 2. showAlert: A function for displaying user-friendly alerts (e.g., a custom modal or a simple browser alert).
//    This is imported from '../util.js'.

import { API_BASE_URL } from '../config/config.js';
import { showAlert } from '../util.js';

const APPOINTMENT_API = `${API_BASE_URL}/appointments`; // Base endpoint for appointment-related operations

/**
 * Fetches appointments for the logged-in doctor based on optional date and patient name filters.
 *
 * @param {string} [date='null'] - The date for which to retrieve appointments in 'YYYY-MM-DD' format.
 * Defaults to 'null' if no specific date filter is applied.
 * @param {string} [patientName='null'] - The name of the patient to filter appointments by.
 * Defaults to 'null' if no patient name filter is applied.
 * @param {string} token - The authentication token of the logged-in doctor. This is required for authorization.
 * @returns {Promise<Array<Object>>} - A promise that resolves to an array of appointment objects,
 * or an empty array if an error occurs or no appointments are found.
 */
export async function getAllAppointments(date = 'null', patientName = 'null', token) {
    // Format date and patientName to 'null' strings if they are empty or not provided,
    // as the backend expects 'null' as a path variable for optional parameters.
    const formattedDate = date && date.trim() !== '' ? date : 'null';
    const formattedPatientName = patientName && patientName.trim() !== '' ? encodeURIComponent(patientName.trim()) : 'null';

    // Construct the full URL for the API call.
    // The structure matches the backend endpoint: /appointments/{dateStr}/{patientName}/{token}
    const url = `${APPOINTMENT_API}/${formattedDate}/${formattedPatientName}/${token}`;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.appointments || [];
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for get all appointments:", jsonError);
                showAlert(`Server error (Status: ${response.status}) during appointment fetch.`);
                return [];
            }
            console.error("Failed to fetch appointments:", errorData.message || "No error message provided from server.");
            showAlert(`Error: ${errorData.message || "Failed to fetch appointments."}`);
            return [];
        }
    } catch (error) {
        console.error("Network error while fetching appointments:", error);
        showAlert("Failed to connect to the server. Please check your network connection.");
        return [];
    }
}

/**
 * Fetches all upcoming appointments for the logged-in doctor, with optional patient name filtering.
 *
 * @param {string} [patientName='null'] - The name of the patient to filter appointments by.
 * Defaults to 'null' if no patient name filter is applied.
 * @param {string} token - The authentication token of the logged-in doctor. This is required for authorization.
 * @returns {Promise<Array<Object>>} - A promise that resolves to an array of upcoming appointment objects,
 * or an empty array if an error occurs or no appointments are found.
 */
export async function getUpcomingAppointments(patientName = 'null', token) {
    const formattedPatientName = patientName && patientName.trim() !== '' ? encodeURIComponent(patientName.trim()) : 'null';

    // Construct the URL to match the new backend endpoint: /appointments/upcoming/{patientName}/{token}
    const url = `${APPOINTMENT_API}/upcoming/${formattedPatientName}/${token}`;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.appointments || [];
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for get upcoming appointments:", jsonError);
                showAlert(`Server error (Status: ${response.status}) during upcoming appointment fetch.`);
                return [];
            }
            console.error("Failed to fetch upcoming appointments:", errorData.message || "No error message provided from server.");
            showAlert(`Error: ${errorData.message || "Failed to fetch upcoming appointments."}`);
            return [];
        }
    } catch (error) {
        console.error("Network error while fetching upcoming appointments:", error);
        showAlert("Failed to connect to the server. Please check your network connection.");
        return [];
    }
}
