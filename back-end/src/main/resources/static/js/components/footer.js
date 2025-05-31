// src/main/resources/static/js/components/footer.js

export function renderFooter() {
    const footer = document.createElement('footer');
    footer.classList.add('app-footer'); // Add a class for styling
    footer.innerHTML = `
        <div class="footer-content">
            <p>&copy; ${new Date().getFullYear()} Clinic Management System. All rights reserved.</p>
            <nav class="footer-nav">
                <ul>
                    <li><a href="#">Privacy Policy</a></li>
                    <li><a href="#">Terms of Service</a></li>
                    <li><a href="#">Contact Us</a></li>
                </ul>
            </nav>
        </div>
    `;
    document.body.appendChild(footer); // Add footer to the bottom of the body
}
