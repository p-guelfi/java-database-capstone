// src/main/resources/static/js/components/doctorCard.js

export function createDoctorCard(doctorData, userRole) {
    const card = document.createElement('div');
    card.classList.add('doctor-card');
    card.dataset.doctorId = doctorData.id; // Store doctor ID for actions

    card.innerHTML = `
        <h3>${doctorData.name}</h3>
        <p><strong>Email:</strong> ${doctorData.email}</p>
        <p><strong>Specialty:</strong> ${doctorData.specialty || 'N/A'}</p>
        <p><strong>Phone:</strong> ${doctorData.phone || 'N/A'}</p>
        <p><strong>Bio:</strong> ${doctorData.bio || 'No bio provided.'}</p>
        <div class="card-actions">
            ${userRole === 'admin' ? `
                <button class="delete-doctor-btn" data-id="${doctorData.id}">Delete</button>
                <button class="edit-doctor-btn" data-id="${doctorData.id}">Edit</button>
            ` : ''}
            ${userRole === 'patient' ? `
                <button class="book-appointment-btn" data-id="${doctorData.id}">Book Appointment</button>
            ` : ''}
        </div>
    `;

    // Event listeners for buttons (will be handled by parent page's JS logic)
    // You'll need to attach specific handlers in adminDashboard.js/patientDashboard.js
    // For example:
    // card.querySelector('.delete-doctor-btn')?.addEventListener('click', handleDeleteDoctor);
    // card.querySelector('.book-appointment-btn')?.addEventListener('click', handleBookAppointment);

    return card;
}
