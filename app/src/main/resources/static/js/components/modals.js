// modals.js
// This file handles the display and content injection for a generic modal window.

import { generateFormHtml } from '../util.js'; // Import utility to generate forms

/**
 * Global variables for modal elements.
 */
const modal = document.getElementById('modal');
const closeModalBtn = document.getElementById('closeModal');
const modalBody = document.getElementById('modal-body');

// Attach event listener to close button (if modal and close button exist)
if (closeModalBtn) {
    closeModalBtn.addEventListener('click', closeModal);
}

// Close modal if user clicks outside of it
if (modal) {
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
}

/**
 * Opens the generic modal and injects content based on the modalType.
 * @param {string} modalType - The type of modal to open (e.g., 'adminLogin', 'addDoctor', 'patientLogin', 'patientSignUp', 'viewPrescription', 'addPrescription', 'bookingOverlay').
 * @param {Object} [data] - Optional data to pass to the modal for content generation (e.g., doctor object for booking, patient data for prescriptions).
 */
export function openModal(modalType, data = {}) {
    if (!modal || !modalBody) {
        console.error("Modal elements not found. Cannot open modal.");
        return;
    }

    modalBody.innerHTML = ''; // Clear previous modal content

    let modalContent = '';
    let formFields = [];
    let submitHandler = null;

    switch (modalType) {
        case 'adminLogin':
            modalContent = '<h3>Admin Login</h3>';
            formFields = [
                { id: 'adminUsername', name: 'Username', type: 'text', placeholder: 'Enter username', required: true },
                { id: 'adminPassword', name: 'Password', type: 'password', placeholder: 'Enter password', required: true }
            ];
            modalContent += generateFormHtml(formFields, 'Login', 'adminLoginForm');
            // Attach specific login handler after modal is shown
            modal.style.display = 'flex'; // Display as flex to center content
            // Need to defer handler attachment to ensure form is in DOM
            setTimeout(() => {
                const adminLoginForm = document.getElementById('adminLoginForm');
                if (adminLoginForm) {
                    adminLoginForm.addEventListener('submit', (event) => {
                        event.preventDefault(); // Prevent default form submission
                        // This will be handled by the global function in index.js
                        window.adminLoginHandler();
                    });
                }
            }, 0);
            break;

        case 'doctorLogin':
            modalContent = '<h3>Doctor Login</h3>';
            formFields = [
                { id: 'doctorEmail', name: 'Email', type: 'email', placeholder: 'Enter email', required: true },
                { id: 'doctorPassword', name: 'Password', type: 'password', placeholder: 'Enter password', required: true }
            ];
            modalContent += generateFormHtml(formFields, 'Login', 'doctorLoginForm');
            modal.style.display = 'flex';
            setTimeout(() => {
                const doctorLoginForm = document.getElementById('doctorLoginForm');
                if (doctorLoginForm) {
                    doctorLoginForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        // This will be handled by the global function in index.js
                        window.doctorLoginHandler();
                    });
                }
            }, 0);
            break;

        case 'patientLogin':
            modalContent = '<h3>Patient Login</h3>';
            formFields = [
                { id: 'patientLoginEmail', name: 'Email', type: 'email', placeholder: 'Enter email', required: true },
                { id: 'patientLoginPassword', name: 'Password', type: 'password', placeholder: 'Enter password', required: true }
            ];
            modalContent += generateFormHtml(formFields, 'Login', 'patientLoginForm');
            modal.style.display = 'flex';
            setTimeout(() => {
                const patientLoginForm = document.getElementById('patientLoginForm');
                if (patientLoginForm) {
                    patientLoginForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        // This will be handled by a function in patientServices.js and called in index.js or patientDashboard.js
                        // Assuming a global or imported handler: window.patientLoginHandler();
                    });
                }
            }, 0);
            break;

        case 'patientSignUp':
            modalContent = '<h3>Patient Sign Up</h3>';
            formFields = [
                { id: 'patientName', name: 'Full Name', type: 'text', placeholder: 'Enter full name', required: true },
                { id: 'patientEmail', name: 'Email', type: 'email', placeholder: 'Enter email', required: true },
                { id: 'patientPassword', name: 'Password', type: 'password', placeholder: 'Create password', required: true },
                { id: 'patientPhone', name: 'Phone Number', type: 'tel', placeholder: 'Enter 10-digit phone number', required: true },
                { id: 'patientAddress', name: 'Address', type: 'text', placeholder: 'Enter address', required: true }
            ];
            modalContent += generateFormHtml(formFields, 'Sign Up', 'patientSignUpForm');
            modal.style.display = 'flex';
            setTimeout(() => {
                const patientSignUpForm = document.getElementById('patientSignUpForm');
                if (patientSignUpForm) {
                    patientSignUpForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        // Assuming a global or imported handler for patient signup
                        // window.patientSignupHandler();
                    });
                }
            }, 0);
            break;

        case 'addDoctor':
            modalContent = '<h3>Add New Doctor</h3>';
            formFields = [
                { id: 'doctorName', name: 'Name', type: 'text', placeholder: 'Doctor\'s full name', required: true },
                { id: 'doctorSpecialty', name: 'Specialty', type: 'text', placeholder: 'e.g., Cardiology', required: true },
                { id: 'doctorEmail', name: 'Email', type: 'email', placeholder: 'Doctor\'s email', required: true },
                { id: 'doctorPassword', name: 'Password', type: 'password', placeholder: 'Initial password', required: true },
                { id: 'doctorPhone', name: 'Phone', type: 'tel', placeholder: '10-digit phone number', required: true },
                {
                    id: 'doctorAvailableTimes', name: 'Available Times (select all that apply)', type: 'checkbox-group',
                    options: [
                        { value: '09:00 - 10:00', text: '09:00 - 10:00' },
                        { value: '10:00 - 11:00', text: '10:00 - 11:00' },
                        { value: '11:00 - 12:00', text: '11:00 - 12:00' },
                        { value: '13:00 - 14:00', text: '13:00 - 14:00' },
                        { value: '14:00 - 15:00', text: '14:00 - 15:00' },
                        { value: '15:00 - 16:00', text: '15:00 - 16:00' }
                    ]
                }
            ];
            modalContent += generateFormHtml(formFields, 'Add Doctor', 'addDoctorForm');
            modal.style.display = 'flex';
            setTimeout(() => {
                const addDoctorForm = document.getElementById('addDoctorForm');
                if (addDoctorForm) {
                    addDoctorForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        // This handler will be in adminDashboard.js
                        if (typeof window.adminAddDoctor === 'function') {
                            window.adminAddDoctor();
                        } else {
                            console.error("adminAddDoctor function not found.");
                        }
                    });
                }
            }, 0);
            break;

        case 'viewPrescription':
            // Data should contain prescription details
            const prescription = data.prescription || {};
            modalContent = `<h3>Prescription Details</h3>
                <p><strong>Patient:</strong> ${prescription.patientName || 'N/A'}</p>
                <p><strong>Appointment ID:</strong> ${prescription.appointmentId || 'N/A'}</p>
                <p><strong>Medication:</strong> ${prescription.medication || 'N/A'}</p>
                <p><strong>Dosage:</strong> ${prescription.dosage || 'N/A'}</p>
                <p><strong>Doctor Notes:</strong> ${prescription.doctorNotes || 'N/A'}</p>`;
            modal.style.display = 'flex';
            break;

        case 'addPrescription':
            // Data should contain patient and appointment info for context
            modalContent = `<h3>Add New Prescription</h3>
                <p><strong>Patient:</strong> ${data.patientName || 'N/A'}</p>
                <p><strong>Appointment ID:</strong> ${data.appointmentId || 'N/A'}</p>`;
            formFields = [
                { id: 'medicationName', name: 'Medication Name', type: 'text', placeholder: 'e.g., Amoxicillin', required: true },
                { id: 'dosage', name: 'Dosage', type: 'text', placeholder: 'e.g., 500mg, 1 tablet daily', required: true },
                { id: 'doctorNotes', name: 'Doctor Notes', type: 'textarea', placeholder: 'Optional notes...', required: false }
            ];
            modalContent += generateFormHtml(formFields, 'Add Prescription', 'addPrescriptionForm');
            modal.style.display = 'flex';
            // Attach handler (will be in doctorDashboard.js)
            setTimeout(() => {
                const addPrescriptionForm = document.getElementById('addPrescriptionForm');
                if (addPrescriptionForm) {
                    addPrescriptionForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        if (typeof window.addPrescriptionHandler === 'function') {
                            window.addPrescriptionHandler(data.patientId, data.appointmentId);
                        } else {
                            console.error("addPrescriptionHandler function not found.");
                        }
                    });
                }
            }, 0);
            break;

        case 'bookingOverlay':
            // data should contain doctor and patient information
            const doctorToBook = data.doctor;
            const patientBooking = data.patient;
            modalContent = `<h3>Book Appointment with Dr. ${doctorToBook.name}</h3>
                            <p><strong>Specialty:</strong> ${doctorToBook.specialty}</p>
                            <p><strong>Your Name:</strong> ${patientBooking.name}</p>
                            <p><strong>Your Email:</strong> ${patientBooking.email}</p>`;
            formFields = [
                { id: 'bookingDate', name: 'Date', type: 'date', required: true },
                { id: 'bookingTime', name: 'Time', type: 'time', required: true },
                { id: 'appointmentNotes', name: 'Notes', type: 'textarea', placeholder: 'Any specific notes for the doctor?', required: false }
            ];
            modalContent += generateFormHtml(formFields, 'Confirm Booking', 'appointmentBookingForm');
            modal.style.display = 'flex';
            // Attach handler (will be in patientAppointment.js or patientDashboard.js)
            setTimeout(() => {
                const bookingForm = document.getElementById('appointmentBookingForm');
                if (bookingForm) {
                    bookingForm.addEventListener('submit', (event) => {
                        event.preventDefault();
                        if (typeof window.bookAppointmentHandler === 'function') {
                            window.bookAppointmentHandler(doctorToBook.id, patientBooking.id);
                        } else {
                            console.error("bookAppointmentHandler function not found.");
                        }
                    });
                }
            }, 0);
            break;

        default:
            modalContent = '<h3>Unknown Modal Type</h3><p>Content not found for this modal type.</p>';
            modal.style.display = 'flex';
            break;
    }

    modalBody.innerHTML = modalContent;
    modal.style.display = 'flex'; // Ensure modal is visible
}


/**
 * Closes the generic modal.
 */
export function closeModal() {
    if (modal) {
        modal.style.display = 'none';
        modalBody.innerHTML = ''; // Clear content when closing
    }
}
