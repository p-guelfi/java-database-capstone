// util.js
// This file provides utility functions shared across the frontend application.

/**
 * Displays a custom alert message.
 * TODO: Replace with a more sophisticated modal-based alert system for better UX.
 * @param {string} message - The message to display.
 */
export function showAlert(message) {
    // For now, using native alert for simplicity as per existing code TODOs.
    // In a real application, this would trigger a custom modal.
    alert(message);
}

/**
 * Stores a key-value pair in localStorage.
 * @param {string} key - The key to store the value under.
 * @param {string} value - The value to store.
 */
export function setLocalStorageItem(key, value) {
    try {
        localStorage.setItem(key, value);
    } catch (error) {
        console.error(`Error saving to localStorage for key '${key}':`, error);
        showAlert("Failed to save data locally. Please try again.");
    }
}

/**
 * Retrieves a value from localStorage based on its key.
 * @param {string} key - The key of the item to retrieve.
 * @returns {string|null} - The retrieved value, or null if not found.
 */
export function getLocalStorageItem(key) {
    try {
        return localStorage.getItem(key);
    } catch (error) {
        console.error(`Error retrieving from localStorage for key '${key}':`, error);
        return null;
    }
}

/**
 * Removes an item from localStorage based on its key.
 * @param {string} key - The key of the item to remove.
 */
export function removeLocalStorageItem(key) {
    try {
        localStorage.removeItem(key);
    } catch (error) {
        console.error(`Error removing from localStorage for key '${key}':`, error);
        showAlert("Failed to clear local data. Please try again.");
    }
}

/**
 * Formats a given date string or Date object into a readable date string (e.g., "YYYY-MM-DD").
 * @param {string|Date} dateInput - The date string or Date object to format.
 * @returns {string} - The formatted date string, or an empty string if invalid.
 */
export function formatDate(dateInput) {
    if (!dateInput) return '';
    try {
        const date = new Date(dateInput);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    } catch (error) {
        console.error("Error formatting date:", error);
        return '';
    }
}

/**
 * Formats a given date string or Date object into a readable time string (e.g., "HH:MM").
 * @param {string|Date} dateInput - The date string or Date object to format.
 * @returns {string} - The formatted time string, or an empty string if invalid.
 */
export function formatTime(dateInput) {
    if (!dateInput) return '';
    try {
        const date = new Date(dateInput);
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    } catch (error) {
        console.error("Error formatting time:", error);
        return '';
    }
}

/**
 * Generates a form HTML structure based on provided fields.
 * Useful for modals (e.g., add doctor, add patient).
 * @param {Array<Object>} fields - Array of field objects {id, name, type, placeholder, required, value, options (for select)}
 * @param {string} buttonText - Text for the submit button.
 * @param {string} formId - ID for the form element.
 * @returns {string} - HTML string for the form.
 */
export function generateFormHtml(fields, buttonText, formId) {
    let formHtml = `<form id="${formId}">`;
    fields.forEach(field => {
        formHtml += `<div class="form-group">`;
        formHtml += `<label for="${field.id}">${field.name}:</label>`;
        if (field.type === 'select') {
            formHtml += `<select id="${field.id}" class="input-field" ${field.required ? 'required' : ''}>`;
            field.options.forEach(option => {
                formHtml += `<option value="${option.value}" ${field.value === option.value ? 'selected' : ''}>${option.text}</option>`;
            });
            formHtml += `</select>`;
        } else if (field.type === 'checkbox-group') {
            formHtml += `<div class="checkbox-group">`;
            field.options.forEach(option => {
                formHtml += `<label><input type="checkbox" name="${field.id}" value="${option.value}" ${option.checked ? 'checked' : ''}> ${option.text}</label>`;
            });
            formHtml += `</div>`;
        } else {
            formHtml += `<input type="${field.type}" id="${field.id}" class="input-field" placeholder="${field.placeholder || ''}" ${field.required ? 'required' : ''} value="${field.value || ''}">`;
        }
        formHtml += `</div>`;
    });
    formHtml += `<button type="submit" class="button submit-btn">${buttonText}</button>`;
    formHtml += `</form>`;
    return formHtml;
}
