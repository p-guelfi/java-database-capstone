package com.project.back_end.service;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.concurrent.TimeUnit; // For clarity with expiration time

@Component // Marks this class as a Spring component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}") // Injects the secret key from application.properties
    private String SECRET_KEY;

    // Constructor Injection
    public TokenService(AdminRepository adminRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Generates a JWT token for a given user's ID and role.
     * The token includes the user's email as the subject, ID, role, issued date, and an expiration of 7 days.
     *
     * @param userId The ID of the user (admin, doctor, or patient).
     * @param userRole The role of the user (e.g., "ADMIN", "DOCTOR", "PATIENT").
     * @return The generated JWT token.
     */
    public String generateToken(Long userId, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        String email = "";
        switch (userRole) {
            case "ADMIN":
                Optional<Admin> admin = adminRepository.findById(userId);
                if (admin.isPresent()) email = admin.get().getUsername(); // Assuming username is used as email-like identifier for admin
                break;
            case "DOCTOR":
                Optional<Doctor> doctor = doctorRepository.findById(userId);
                if (doctor.isPresent()) email = doctor.get().getEmail();
                break;
            case "PATIENT":
                Optional<Patient> patient = patientRepository.findById(userId);
                if (patient.isPresent()) email = patient.get().getEmail();
                break;
            default:
                throw new IllegalArgumentException("Unknown user role: " + userRole);
        }

        return Jwts.builder()
                .claims(claims) // Add custom claims
                .subject(email) // Set email as the subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))) // 7 days expiration
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token The JWT token.
     * @return All claims contained within the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts a specific claim from a JWT token using a resolver function.
     *
     * @param token The JWT token.
     * @param claimsResolver A function to resolve the desired claim from the Claims object.
     * @param <T> The type of the claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token The JWT token from which the email is to be extracted.
     * @return The email extracted from the token.
     */
    public String getUserEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from a JWT token.
     * Assumes "userId" is stored as a custom claim.
     *
     * @param token The JWT token.
     * @return The user ID extracted from the token.
     */
    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extracts the user role from a JWT token.
     * Assumes "userRole" is stored as a custom claim.
     *
     * @param token The JWT token.
     * @return The user role extracted from the token.
     */
    public String getUserRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("userRole", String.class));
    }

    /**
     * Checks if the token is expired.
     *
     * @param token The JWT token.
     * @return True if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Validates the JWT token for a given user's ID and role.
     * It checks token validity, expiration, and if the extracted user ID and role match the expected ones.
     *
     * @param token The JWT token to be validated.
     * @param expectedUserId The expected ID of the user.
     * @param expectedUserRole The expected role of the user (e.g., "ADMIN", "DOCTOR", "PATIENT").
     * @return True if the token is valid for the specified user ID and role, false otherwise.
     */
    public boolean validateToken(String token, Long expectedUserId, String expectedUserRole) {
        try {
            Long userIdFromToken = getUserIdFromToken(token);
            String userRoleFromToken = getUserRoleFromToken(token);
            String emailFromToken = getUserEmailFromToken(token);

            if (!userIdFromToken.equals(expectedUserId) || !userRoleFromToken.equals(expectedUserRole)) {
                return false; // Mismatch in user ID or role
            }

            // Verify if the user (identified by ID and role) still exists in the database
            // This adds an extra layer of security, useful if a user is deleted after token issuance
            boolean userExists = false;
            switch (expectedUserRole) {
                case "ADMIN":
                    userExists = adminRepository.findById(expectedUserId).isPresent();
                    break;
                case "DOCTOR":
                    userExists = doctorRepository.findById(expectedUserId).isPresent();
                    break;
                case "PATIENT":
                    userExists = patientRepository.findById(expectedUserId).isPresent();
                    break;
                default:
                    return false; // Unknown role
            }

            if (!userExists) {
                return false; // User no longer exists in the database
            }

            return !isTokenExpired(token); // Final check for expiration
        } catch (Exception e) {
            // Handle parsing errors, signature validation failures, etc.
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the signing key used for JWT token signing.
     *
     * @return The key used for signing the JWT.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}