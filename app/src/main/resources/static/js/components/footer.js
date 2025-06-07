// footer.js
// This file defines and renders a consistent footer component for all pages.

/**
 * Renders the static footer content into the #footer div.
 */
export function renderFooter() {
    const footerDiv = document.getElementById("footer");
    if (!footerDiv) {
        console.error("Footer div with ID 'footer' not found.");
        return;
    }

    footerDiv.innerHTML = `
        <footer class="footer">
            <div class="footer-logo">
                <img src="./assets/images/logo/logo.png" alt="Smart Clinic Logo">
                <p>&copy; Copyright 2025 Smart Clinic. All rights reserved.</p>
            </div>
            <div class="footer-column">
                <h4>Company</h4>
                <ul>
                    <li><a href="#">About Us</a></li>
                    <li><a href="#">Careers</a></li>
                    <li><a href="#">Press</a></li>
                </ul>
            </div>
        </footer>
    `;
}

// Call the function to render the footer when the script loads
document.addEventListener('DOMContentLoaded', renderFooter);
