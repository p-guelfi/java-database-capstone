// src/main/resources/static/js/services/doctorServices.js

import { API_BASE_URL } from '../config/config.js';

const DOCTORS_API_URL = `${API_BASE_URL}/doctors`; // Assuming your backend has a /doctors endpoint

// Function to get all doctors
export async function getDoctors(token) {
    try {
        const response = await fetch(DOCTORS_API_URL, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // Include auth token
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert('Session expired or unauthorized. Please log in again.');
                window.location.href = '/index.html'; // Redirect to login
                return [];
            }
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to fetch doctors.');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching doctors:', error);
        alert(`Error fetching doctors: ${error.message}`);
        return []; // Return empty array on error
    }
}

// Function to save (add/update) a doctor
export async function saveDoctor(doctorData, token) {
    const method = doctorData.id ? 'PUT' : 'POST'; // Use PUT for update if ID exists, POST for new
    const url = doctorData.id ? `<span class="math-inline">\{DOCTORS\_API\_URL\}/</span>{doctorData.id}` : DOCTORS_API_URL;

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(doctorData)
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert('Session expired or unauthorized. Please log in again.');
                window.location.href = '/index.html';
                return null;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || `Failed to ${method === 'POST' ? 'add' : 'update'} doctor.`);
        }
        return await response.json(); // Return the saved doctor object
    } catch (error) {
        console.error(`Error saving doctor:`, error);
        alert(`Error saving doctor: ${error.message}`);
        return null; // Return null on error
    }
}

// Function to filter doctors (client-side filtering for simplicity, or can be API call)
// This example assumes getDoctors fetches all, and then filters client-side.
// If your backend has a filter endpoint, this would be an API call.
export async function filterDoctors(doctors, searchTerm = '', specialty = '') {
    let filtered = doctors;

    if (searchTerm) {
        const lowerCaseSearchTerm = searchTerm.toLowerCase();
        filtered = filtered.filter(doctor =>
            (doctor.name && doctor.name.toLowerCase().includes(lowerCaseSearchTerm)) ||
            (doctor.email && doctor.email.toLowerCase().includes(lowerCaseSearchTerm)) ||
            (doctor.specialty && doctor.specialty.toLowerCase().includes(lowerCaseSearchTerm)) ||
            (doctor.phone && doctor.phone.includes(searchTerm))
        );
    }

    if (specialty && specialty !== 'All') {
        filtered = filtered.filter(doctor =>
            doctor.specialty && doctor.specialty.toLowerCase() === specialty.toLowerCase()
        );
    }

    return filtered;
}


// Function to delete a doctor
export async function deleteDoctor(doctorId, token) {
    try {
        const response = await fetch(`<span class="math-inline">\{DOCTORS\_API\_URL\}/</span>{doctorId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert('Session expired or unauthorized. Please log in again.');
                window.location.href = '/index.html';
                return false;
            }
            const errorData = await response.json(); // Try to parse error message
            throw new Error(errorData.message || 'Failed to delete doctor.');
        }
        // For DELETE, response.json() might fail if no content, so check
        return response.status === 204 || response.ok; // 204 No Content is common for successful delete
    } catch (error) {
        console.error(`Error deleting doctor with ID ${doctorId}:`, error);
        alert(`Error deleting doctor: ${error.message}`);
        return false; // Return false on error
    }
}
