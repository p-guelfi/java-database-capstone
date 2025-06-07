package com.project.back_end.service;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.mysql.AdminRepository;
import com.project.back_end.repository.mysql.DoctorRepository;
import com.project.back_end.repository.mysql.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct; // Use jakarta.annotation.PostConstruct for Spring Boot 3+
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey; // Use javax.crypto.SecretKey for Spring Boot 3+
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service class to handle JWT token generation, parsing, and validation.
 * This class ensures secure authentication and authorization by managing JWTs.
 */
@Service
public class TokenService {

    // The secret key for signing JWTs. Loaded from application.properties.
    // It should be a strong, random string of at least 32 characters for HS256.
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey; // The actual secret key object used for signing/verification

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Initializes the signing key using the 'jwt.secret' configured in application.properties.
     * This method is called automatically by Spring after dependency injection.
     * It ensures the secret is robust enough for JWT signing.
     */
    @PostConstruct
    public void init() {
        try {
            // Attempt to decode the secret as a Base64-encoded key. This is recommended for production.
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            // You can also Base64 encode a randomly generated key (e.g., Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded())
            // and put that Base64 string in your application.properties for robustness.
        } catch (IllegalArgumentException e) {
            // This can happen if the secret is not a valid Base64 string, or is too short.
            System.err.println("WARNING: Configured JWT secret might be invalid or too short. Generating a new secure key for development.");
            this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Fallback to a securely generated key
            // In a production environment, you would likely want to fail fast here or provide a more robust key management solution.
        } catch (Exception e) {
            System.err.println("Error initializing JWT signing key: " + e.getMessage());
            this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Fallback
        }
    }

    /**
     * Generates a JWT token for a given user.
     * The token includes the user's email as the subject, their userId, role,
     * issued date, and an expiration of 7 days.
     *
     * @param email The email (or username for admin) of the user.
     * @param userId The ID of the user (Admin, Doctor, or Patient).
     * @param role The role of the user (e.g., "admin", "doctor", "patient").
     * @return The generated JWT token string.
     */
    public String generateToken(String email, Long userId, String role) {
        Date now = new Date();
        long expirationMillis = TimeUnit.DAYS.toMillis(7); // Token valid for 7 days
        Date expirationDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(email) // Typically the user's identifier
                .claim("userId", userId) // Custom claim for user ID
                .claim("role", role) // Custom claim for user role
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(signingKey, SignatureAlgorithm.HS256) // Sign with the initialized key and algorithm
                .compact(); // Build and compact the JWT to a string
    }

    /**
     * Parses a JWT token and returns its claims (payload).
     * @param token The JWT token string.
     * @return The Claims object representing the token's payload.
     * @throws ExpiredJwtException if the token is expired.
     * @throws MalformedJwtException if the token is not a valid JWT.
     * @throws SignatureException if the token's signature is invalid.
     * @throws IllegalArgumentException if the token is null, empty, or an invalid format.
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey) // Set the signing key for verification
                .build()
                .parseClaimsJws(token) // Parse and validate the token
                .getBody(); // Get the claims (payload)
    }

    /**
     * Extracts the email (subject) from a JWT token.
     * @param token The JWT token string.
     * @return The email extracted from the token.
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired.
     */
    public String extractEmail(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extracts the user ID from a given token's claims.
     * @param token The JWT token string.
     * @return The user ID from the token's claims, or null if the token is invalid or the claim is missing/malformed.
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            Claims claims = parseToken(token);
            // Claims.get() returns an Object. Cast to Number then get longValue().
            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            System.err.println("Error extracting userId from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the user role from a given token's claims.
     * @param token The JWT token string.
     * @return The role from the token's claims, or null if the token is invalid or the claim is missing/malformed.
     */
    public String getUserRoleFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            System.err.println("Error extracting role from token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates a JWT token for a given user ID and expected role.
     * This method verifies the token's integrity, expiration, and ensures that
     * the user's ID and role embedded in the token match the expected values and
     * correspond to an existing user in the database.
     *
     * @param token The JWT token to be validated.
     * @param userId The ID of the user whose token is being validated.
     * @param expectedRole The role expected for this token (e.g., "admin", "doctor", "patient").
     * @return true if the token is valid for the specified user and role, false otherwise.
     */
    public boolean validateTokenForUser(String token, Long userId, String expectedRole) {
        if (token == null || userId == null || expectedRole == null) {
            return false;
        }
        try {
            Claims claims = parseToken(token); // This will throw if token is invalid or expired

            Long userIdFromToken = ((Number) claims.get("userId")).longValue();
            String roleInToken = (String) claims.get("role");
            String subjectEmailOrUsername = claims.getSubject();

            // 1. Check if user ID and role in token match expected values
            if (!userId.equals(userIdFromToken) || !expectedRole.equalsIgnoreCase(roleInToken)) {
                System.err.println("Token validation failed: User ID (" + userIdFromToken + " vs expected " + userId + ") or role (" + roleInToken + " vs expected " + expectedRole + ") mismatch.");
                return false;
            }

            // 2. Verify that the user still exists in the database for the given role and ID/email
            switch (expectedRole.toLowerCase()) {
                case "admin":
                    Optional<Admin> admin = adminRepository.findById(userId);
                    return admin.isPresent() && admin.get().getUsername().equals(subjectEmailOrUsername);
                case "doctor":
                    Optional<Doctor> doctor = doctorRepository.findById(userId);
                    return doctor.isPresent() && doctor.get().getEmail().equals(subjectEmailOrUsername);
                case "patient":
                    Optional<Patient> patient = patientRepository.findById(userId);
                    return patient.isPresent() && patient.get().getEmail().equals(subjectEmailOrUsername);
                default:
                    System.err.println("Token validation failed: Unknown expected role: " + expectedRole);
                    return false;
            }
        } catch (ExpiredJwtException e) {
            System.err.println("JWT Validation Error: Token expired. " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("JWT Validation Error: Malformed token. " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("JWT Validation Error: Invalid signature. " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT Validation Error: Unsupported token. " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT Validation Error: Illegal argument (e.g., token null/empty). " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("JWT Validation Error: An unexpected error occurred. " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for unhandled exceptions during token processing
            return false;
        }
    }
}
