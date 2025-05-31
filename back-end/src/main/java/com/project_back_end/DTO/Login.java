package com.project_back_end.DTO; // Assuming this is still your base package for DTOs

// If you chose to put DTOs in a sub-package like 'com.project.back_end.dto',
// then change the package declaration accordingly:
// package com.project.back_end.dto;

public class Login {

    private String email;
    private String password;

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Optional: You might want a default constructor if using frameworks that
    // require it for deserialization, though Spring often handles it.
    public Login() {
    }

    // Optional: A constructor for convenience, though for a DTO used in @RequestBody,
    // Spring typically relies on the no-arg constructor and setters.
    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }
}