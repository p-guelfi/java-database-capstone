// src/main/resources/static/js/services/patientServices.js

import { API_BASE_URL } from '../config/config.js';

// Helper to get authorization header
function getAuthHeaders() {
    const token = localStorage.getItem('doctorToken');
    if (!token) {
        alert('Authentication token not found. Please log in.');
        window.location.href = '/pages/defineRole.html'; // Redirect to login
        return null;
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

// Method to get all patient appointments for today
export async function getPatientsForToday() {
    const headers = getAuthHeaders();
    if (!headers) return { success: false, data: [] };

    try {
        const response = await fetch(`${API_BASE_URL}/api/appointments/today`, {
            method: 'GET',
            headers: headers
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, data: data };
        } else {
            const errorData = await response.json();
            console.error('Failed to fetch patients for today:', errorData);
            return { success: false, message: errorData.message || 'Failed to fetch patients for today.' };
        }
    } catch (error) {
        console.error('Error fetching patients for today:', error);
        return { success: false, message: 'Network error or server unavailable.' };
    }
}

// Method to get all appointments for a specific date
export async function getPatientsByDate(date) { // date should be in YYYY-MM-DD format
    const headers = getAuthHeaders();
    if (!headers) return { success: false, data: [] };

    try {
        const response = await fetch(`<span class="math-inline">\{API\_BASE\_URL\}/api/appointments/by\-date?date\=</span>{date}`, {
            method: 'GET',
            headers: headers
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, data: data };
        } else {
            const errorData = await response.json();
            console.error(`Failed to fetch patients for date ${date}:`, errorData);
            return { success: false, message: errorData.message || `Failed to fetch patients for date ${date}.` };
        }
    } catch (error) {
        console.error(`Error fetching patients for date ${date}:`, error);
        return { success: false, message: 'Network error or server unavailable.' };
    }
}

// Placeholder for saving/updating prescription if needed in a modal
export async function savePrescription(patientId, prescriptionDetails) {
    const headers = getAuthHeaders();
    if (!headers) return { success: false };

    try {
        const response = await fetch(`<span class="math-inline">\{API\_BASE\_URL\}/api/patients/</span>{patientId}/prescription`, {
            method: 'PUT', // or POST depending on your backend
            headers: headers,
            body: JSON.stringify({ prescription: prescriptionDetails })
        });

        if (response.ok) {
            return { success: true, message: 'Prescription saved successfully.' };
        } else {
            const errorData = await response.json();
            console.error('Failed to save prescription:', errorData);
            return { success: false, message: errorData.message || 'Failed to save prescription.' };
        }
    } catch (error) {
        console.error('Error saving prescription:', error);
        return { success: false, message: 'Network error or server unavailable.' };
    }
}
