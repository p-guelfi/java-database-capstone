package com.project.back_end.dto;

/**
 * Data Transfer Object (DTO) for handling user login requests.
 * This class encapsulates the email and password submitted from the frontend
 * for authentication purposes.
 * It is used as a @RequestBody parameter in controller methods.
 */
public class LoginDTO {

    private String email; // The email address of the user attempting to log in
    private String password; // The password provided by the user

    // Default constructor (often needed for deserialization by frameworks like Spring/Jackson)
    public LoginDTO() {
    }

    /**
     * Constructor to initialize LoginDTO with email and password.
     * @param email The user's email.
     * @param password The user's password.
     */
    public LoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getter and Setter Methods ---

    /**
     * Retrieves the email address from the login request.
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for the login request.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the password from the login request.
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the login request.
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginDTO{" +
               "email='" + email + '\'' +
               ", password='[PROTECTED]'" + // Do not log password directly
               '}';
    }
}
