// src/main/resources/static/js/components/patientRows.js

import { openModal } from './modals.js'; // Assuming you have a modals.js for openModal

export function createPatientRow(patient) {
    const tr = document.createElement("tr");
    tr.setAttribute('data-patient-id', patient.id); // Useful for identifying the patient
    tr.innerHTML = `
        <td data-label="Patient ID">${patient.id}</td>
        <td data-label="Name">${patient.name}</td>
        <td data-label="Phone">${patient.phone || 'N/A'}</td>
        <td data-label="Email">${patient.email}</td>
        <td data-label="Prescription">
            <button 
                class="prescription-btn" 
                data-patient-id="${patient.id}" 
                data-patient-name="${patient.name}"
            >
                View/Add
            </button>
        </td>
    `;

    // Add event listener for the prescription button
    const prescriptionBtn = tr.querySelector('.prescription-btn');
    if (prescriptionBtn) {
        prescriptionBtn.addEventListener('click', (event) => {
            const patientId = event.target.getAttribute('data-patient-id');
            const patientName = event.target.getAttribute('data-patient-name');
            document.getElementById('prescriptionPatientName').textContent = patientName;
            document.getElementById('prescriptionPatientId').value = patientId;
            document.getElementById('prescriptionText').value = ''; // Clear previous text
            openModal('prescriptionModal');
        });
    }

    return tr;
}

