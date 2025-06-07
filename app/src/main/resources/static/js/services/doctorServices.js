// doctorServices.js
// This module centralizes all API interactions related to doctor data,
// improving modularity, reusability, and separation of concerns.

import { API_BASE_URL } from '../config/config.js'; // Import the base API URL
import { showAlert } from '../util.js'; // Import custom alert function

const DOCTOR_API = `${API_BASE_URL}/doctor`; // Base endpoint for doctor-related operations

/**
 * Fetches a list of all doctors from the backend. (Public view)
 * @returns {Array<Object>} - An array of doctor objects, or an empty array if an error occurs.
 */
export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.doctors || [];
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for get doctors:", jsonError);
                showAlert(`Server error (Status: ${response.status}) during doctor fetch.`);
                return [];
            }
            console.error("Failed to fetch doctors:", errorData.message || "No error message provided from server.");
            showAlert(`Error: ${errorData.message || "Failed to fetch doctors."}`);
            return [];
        }
    } catch (error) {
        console.error("Network error while fetching doctors:", error);
        showAlert("Failed to connect to the server. Please check your network.");
        return [];
    }
}

/**
 * Fetches a single doctor's profile by their ID. This is for a doctor to retrieve their own profile.
 * @param {Long} doctorId - The ID of the doctor to fetch.
 * @param {string} token - The authentication token of the logged-in doctor.
 * @returns {Object|null} - The doctor's profile object, or null if not found or an error occurs.
 */
export async function getDoctorProfileById(doctorId, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorId}/${token}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.doctor || data;
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error(`Error parsing JSON for get doctor profile (ID: ${doctorId}):`, jsonError);
                showAlert(`Server error (Status: ${response.status}) during doctor profile fetch.`);
                return null;
            }
            console.error(`Failed to fetch doctor profile with ID ${doctorId}:`, errorData.message || "No error message provided.");
            showAlert(`Error: ${errorData.message || "Failed to fetch doctor profile."}`);
            return null;
        }
    } catch (error) {
        console.error(`Network error while fetching doctor profile with ID ${doctorId}:`, error);
        showAlert("Failed to connect to the server. Please check your network for doctor profile.");
        return null;
    }
}

/**
 * Updates a doctor's profile (name, email, phone, specialty).
 * @param {Object} doctorData - The doctor object with updated details (must include ID).
 * @param {string} token - The authentication token of the logged-in doctor.
 * @returns {Object} - An object indicating success status and a message.
 */
export async function updateDoctorProfile(doctorData, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorData.id}/${token}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(doctorData)
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, message: data.message || "Profile updated successfully!" };
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for updateDoctorProfile:", jsonError);
                return { success: false, message: `Server error (Status: ${response.status})` };
            }
            console.error("Failed to update doctor profile:", errorData.message || "No error message provided from server.");
            return { success: false, message: errorData.message || "Failed to update profile." };
        }
    } catch (error) {
        console.error("Network error while updating doctor profile:", error);
        return { success: false, message: "Network error during profile update. Please try again." };
    }
}


/**
 * Fetches all recurring available time slots for a specific doctor.
 * @param {Long} doctorId - The ID of the doctor.
 * @param {string} token - The doctor's authentication token.
 * @returns {Promise<Array<Object>>} - An array of available time slot objects ({id: Long, availableTime: String}), or empty.
 */
export async function getDoctorAvailableTimes(doctorId, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorId}/available-times/${token}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.availableTimes || [];
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error(`Error parsing JSON for get doctor available times (ID: ${doctorId}):`, jsonError);
                showAlert(`Server error (Status: ${response.status}) during available times fetch.`);
                return [];
            }
            console.error(`Failed to fetch doctor available times for ID ${doctorId}:`, errorData.message || "No error message provided.");
            showAlert(`Error: ${errorData.message || "Failed to fetch available times."}`);
            return [];
        }
    } catch (error) {
        console.error(`Network error while fetching doctor available times for ID ${doctorId}:`, error);
        showAlert("Failed to connect to the server. Please check your network for available times.");
        return [];
    }
}

/**
 * Adds a new recurring available time slot for a doctor.
 * @param {Long} doctorId - The ID of the doctor.
 * @param {string} availableTime - The new time slot string (e.g., "09:00-10:00").
 * @param {string} token - The doctor's authentication token.
 * @returns {Object} - An object indicating success status and a message.
 */
export async function addDoctorAvailableTime(doctorId, availableTime, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorId}/available-times/${token}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ availableTime: availableTime }) // Send as JSON object
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, message: data.message || "Available time added successfully!" };
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for addDoctorAvailableTime:", jsonError);
                return { success: false, message: `Server error (Status: ${response.status})` };
            }
            console.error("Failed to add available time:", errorData.message || "No error message provided from server.");
            return { success: false, message: errorData.message || "Failed to add available time." };
        }
    } catch (error) {
        console.error("Network error while adding available time:", error);
        return { success: false, message: "Network error during adding available time. Please try again." };
    }
}

/**
 * Removes a recurring available time slot for a doctor.
 * @param {Long} slotId - The ID of the available time slot record to remove.
 * @param {string} token - The doctor's authentication token.
 * @returns {Object} - An object indicating success status and a message.
 */
export async function removeDoctorAvailableTime(slotId, token) {
    const doctorId = localStorage.getItem("userId"); // Re-get doctorId from localStorage for path
    if (!doctorId) {
        showAlert("Error: Doctor ID not found in local storage.");
        return { success: false, message: "Doctor ID not found." };
    }
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorId}/available-times/${slotId}/${token}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, message: data.message || "Available time removed successfully!" };
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for removeDoctorAvailableTime:", jsonError);
                return { success: false, message: `Server error (Status: ${response.status})` };
            }
            console.error("Failed to remove available time:", errorData.message || "No error message provided from server.");
            return { success: false, message: errorData.message || "Failed to remove available time." };
        }
    } catch (error) {
        console.error("Network error while removing available time:", error);
        return { success: false, message: "Network error during removing available time. Please try again." };
    }
}


/**
 * Deletes a doctor by their ID. Requires an authentication token. (Admin function)
 * @param {string} id - The ID of the doctor to delete.
 * @param {string} token - The authentication token of the admin.
 * @returns {boolean} - True if deletion was successful, false otherwise.
 */
export async function deleteDoctor(id, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${id}/${token}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            return true;
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for delete doctor:", jsonError);
                showAlert(`Server error (Status: ${response.status}) during doctor deletion.`);
                return false;
            }
            console.error(`Failed to delete doctor with ID ${id}:`, errorData.message || "No error message provided from server.");
            showAlert(`Failed to delete doctor: ${errorData.message || "Unknown error."}`);
            return false;
        }
    } catch (error) {
        console.error(`Network error while deleting doctor with ID ${id}:`, error);
        showAlert("Network error during doctor deletion. Please try again.");
        return false;
    }
}

/**
 * Saves (adds) a doctor's record. (Admin function)
 * This function sends a POST request to add a new doctor.
 * @param {Object} doctor - The doctor object to save (e.g., name, specialty, email, password, phone, availableTimes).
 * @param {string} token - The authentication token of the admin.
 * @returns {Object} - An object indicating success status and a message.
 */
export async function saveDoctor(doctor, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${token}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            return { success: true, message: data.message || "Doctor added successfully!" };
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for save doctor:", jsonError);
                return { success: false, message: `Server error (Status: ${response.status})` };
            }
            console.error("Failed to save doctor:", errorData.message || "No error message provided from server.");
            return { success: false, message: errorData.message || "Failed to save doctor." };
        }
    } catch (error) {
        console.error("Network error while saving doctor:", error);
        return { success: false, message: "Network error during doctor save. Please try again." };
    }
}

/**
 * Filters doctors based on name, time availability, and specialty. (Public view)
 * Corrected to always send 'null' string for empty filter values.
 * @param {string} name - Doctor's name (optional).
 * @param {string} time - Available time slot (optional, e.g., "09:00 - 10:00").
 * @param {string} specialty - Doctor's specialty (optional).
 * @returns {Array<Object>} - A filtered array of doctor objects, or an empty array if an error occurs.
 */
export async function filterDoctors(name = '', time = '', specialty = '') { // Default to empty strings
    // Ensure 'null' string is sent for empty filter values in URL path
    const formattedName = name.trim() !== '' ? encodeURIComponent(name.trim()) : 'null';
    const formattedTime = time.trim() !== '' ? encodeURIComponent(time.trim()) : 'null';
    const formattedSpecialty = specialty.trim() !== '' ? encodeURIComponent(specialty.trim()) : 'null';

    const url = `${DOCTOR_API}/filter/${formattedName}/${formattedTime}/${formattedSpecialty}`;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            const data = await response.json();
            return data.doctors || [];
        } else {
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (jsonError) {
                console.error("Error parsing error response JSON for filter doctors:", jsonError);
                showAlert(`Server error (Status: ${response.status}) during doctor filtering.`);
                return [];
            }
            console.error("Failed to filter doctors:", errorData.message || "No error message provided from server.");
            showAlert(`Error filtering doctors: ${errorData.message || "Unknown error."}`);
            return [];
        }
    } catch (error) {
        console.error("Network error while filtering doctors:", error);
        showAlert("Network error during doctor filtering. Please try again.");
        return [];
    }
}
