// patientRows.js
// This module provides functions to create and manage patient appointment rows in the dashboard table.

/**
 * Creates a table row (<tr>) element for a given patient appointment.
 * This row includes patient details, appointment time, status, and notes.
 * Patient contact information and notes are only displayed for confirmed appointments (status === 1).
 * Status "1" will display as "Confirmed". Notes that are null will display as "No Notes".
 *
 * @param {Object} appointment - The appointment object. It should contain:
 * - `id`: The unique ID of the appointment.
 * - `patientId`: The ID of the patient.
 * - `patientName`: The name of the patient.
 * - `patientEmail`: The email of the patient.
 * - `patientPhone`: The phone number of the patient.
 * - `appointmentTime`: The time of the appointment (e.g., "10:00 AM").
 * - `status`: The current status of the appointment (e.g., 1 for "Confirmed", 0 for "Pending").
 * - `notes`: (Optional) Any notes associated with the appointment (can be null or an empty string).
 * @returns {HTMLTableRowElement} The dynamically created table row element.
 */
export function createPatientRow(appointment) {
    const row = document.createElement('tr');
    // Determine if the appointment is confirmed (status 1) to conditionally display patient details.
    const isConfirmed = appointment.status === 1;

    // Helper function to handle notes display: 'No Notes' for explicit null or empty string.
    const getNotesDisplay = (notes) => {
        if (notes === null || (typeof notes === 'string' && notes.trim() === '')) {
            return 'No Notes';
        }
        return notes;
    };

    // Use a template literal to construct the table row HTML.
    // Conditional (ternary) operators are used to show patient details or "Not Confirmed" based on `isConfirmed`.
    // Status "1" is now displayed as "Confirmed".
    // Notes are displayed using the `getNotesDisplay` helper.
    row.innerHTML = `
        <td>${appointment.id || 'N/A'}</td>
        <td>${isConfirmed ? (appointment.patientName || 'N/A') : 'Not Confirmed'}</td>
        <td>${isConfirmed ? (appointment.patientPhone || 'N/A') : 'Not Confirmed'}</td>
        <td>${isConfirmed ? (appointment.patientEmail || 'N/A') : 'Not Confirmed'}</td>
        <td>${appointment.appointmentTime || 'N/A'}</td>
        <td>${isConfirmed ? 'Confirmed' : (appointment.status || 'N/A')}</td> <!-- Changed status display -->
        <td>${isConfirmed ? getNotesDisplay(appointment.notes) : 'Not Confirmed'}</td> <!-- Updated notes display -->
    `;

    return row;
}
