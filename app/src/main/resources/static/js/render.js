// render.js
// This file provides utility functions for rendering UI components and handling page redirection
// based on user roles.

/**
 * Stores the selected user role in localStorage and redirects the user
 * to the appropriate dashboard or login page.
 * @param {string} role - The role selected by the user ('admin', 'doctor', 'patient', 'loggedPatient').
 */
export function selectRole(role) {
    localStorage.setItem("userRole", role); // Save the selected role in local storage

    // Redirect based on the selected role
    switch (role) {
        case 'admin':
            window.location.href = "/templates/admin/adminDashboard.html"; // Thymeleaf template
            break;
        case 'doctor':
            window.location.href = "/templates/doctor/doctorDashboard.html"; // Thymeleaf template
            break;
        case 'patient':
            // For unlogged patients, they go to a page that allows login/signup
            window.location.href = "/pages/patientDashboard.html"; // Static HTML page with login/signup
            break;
        case 'loggedPatient':
            // Logged patients go directly to their dashboard
            window.location.href = "/pages/loggedPatientDashboard.html"; // Static HTML page
            break;
        default:
            window.location.href = "/"; // Fallback to homepage
    }
}

/**
 * Displays a list of doctors on a given container element.
 * @param {HTMLElement} containerElement - The DOM element where doctor cards will be rendered.
 * @param {Array<Object>} doctors - An array of doctor objects to display.
 * @param {Function} createCardFunction - The function used to create a single doctor card (e.g., createDoctorCard).
 */
export function renderDoctorCards(containerElement, doctors, createCardFunction) {
    containerElement.innerHTML = ''; // Clear existing content

    if (!doctors || doctors.length === 0) {
        const noDoctorsMessage = document.createElement('p');
        noDoctorsMessage.classList.add('no-records-message');
        noDoctorsMessage.textContent = "No doctors found.";
        containerElement.appendChild(noDoctorsMessage);
        return;
    }

    doctors.forEach(doctor => {
        const doctorCard = createCardFunction(doctor); // Create a card for each doctor
        containerElement.appendChild(doctorCard); // Append the card to the container
    });
}
