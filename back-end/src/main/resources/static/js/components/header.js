// src/main/resources/static/js/components/header.js

export function renderHeader() {
    const header = document.createElement('header');
    header.classList.add('app-header'); // Add a class for styling

    const logoDiv = document.createElement('div');
    logoDiv.classList.add('logo');
    const logoImg = document.createElement('img');
    logoImg.src = 'assets/images/logo/logo.png'; // Path to your logo image
    logoImg.alt = 'Clinic Logo';
    const siteTitle = document.createElement('h1');
    siteTitle.textContent = 'Clinic System';
    logoDiv.appendChild(logoImg);
    logoDiv.appendChild(siteTitle);

    const nav = document.createElement('nav');
    const ul = document.createElement('ul');

    const userRole = localStorage.getItem('selectedRole');
    const adminToken = localStorage.getItem('adminToken');
    const doctorToken = localStorage.getItem('doctorToken');
    const patientToken = localStorage.getItem('patientToken'); // Assuming patient also gets a token

    let isLoggedIn = false;
    if (userRole === 'admin' && adminToken) isLoggedIn = true;
    if (userRole === 'doctor' && doctorToken) isLoggedIn = true;
    if (userRole === 'patient' && patientToken) isLoggedIn = true;

    // Common links (e.g., Home)
    let homeLink = document.createElement('li');
    let homeAnchor = document.createElement('a');
    homeAnchor.href = '/index.html';
    homeAnchor.textContent = 'Home';
    homeLink.appendChild(homeAnchor);
    ul.appendChild(homeLink);

    // Dynamic navigation based on role and login state
    if (isLoggedIn) {
        let dashboardLink = document.createElement('li');
        let dashboardAnchor = document.createElement('a');
        if (userRole === 'admin') {
            dashboardAnchor.href = 'templates/admin/adminDashboard.html';
            dashboardAnchor.textContent = 'Admin Dashboard';
        } else if (userRole === 'doctor') {
            dashboardAnchor.href = 'templates/doctor/doctorDashboard.html';
            dashboardAnchor.textContent = 'Doctor Dashboard';
        } else if (userRole === 'patient') {
            dashboardAnchor.href = 'pages/patientDashboard.html'; // Assuming patient dashboard is in /pages
            dashboardAnchor.textContent = 'Patient Dashboard';
        }
        dashboardLink.appendChild(dashboardAnchor);
        ul.appendChild(dashboardLink);

        let logoutLink = document.createElement('li');
        let logoutButton = document.createElement('button');
        logoutButton.textContent = 'Logout';
        logoutButton.classList.add('logout-button'); // Add class for styling
        logoutButton.addEventListener('click', () => {
            // Clear all relevant localStorage items
            localStorage.removeItem('adminToken');
            localStorage.removeItem('doctorToken');
            localStorage.removeItem('patientToken');
            localStorage.removeItem('selectedRole');
            // Redirect to login page
            window.location.href = '/index.html';
        });
        logoutLink.appendChild(logoutButton);
        ul.appendChild(logoutLink);

    } else {
        // If not logged in, maybe show "Login" button or nothing here,
        // as index.html already handles initial role selection
    }

    nav.appendChild(ul);
    header.appendChild(logoDiv);
    header.appendChild(nav);

    document.body.prepend(header); // Add header to the very top of the body
}
