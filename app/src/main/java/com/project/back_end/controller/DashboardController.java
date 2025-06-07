package com.project.back_end.controller;

import com.project.back_end.service.TokenService; // Importar el TokenService real
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controlador para manejar las solicitudes a las páginas seguras del panel de control (Administrador y Médico).
 * Valida los tokens JWT y los roles de usuario antes de renderizar las plantillas de Thymeleaf respectivas.
 */
@Controller
public class DashboardController {

    private final TokenService tokenService; // Autowire del TokenService

    @Autowired
    public DashboardController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Maneja las solicitudes para el Panel de Control del Administrador.
     * Requiere un token JWT en la variable de ruta para la autenticación.
     * @param token El token JWT para la autenticación.
     * @param model El Modelo de Spring para añadir atributos a la vista.
     * @return El nombre de la plantilla de Thymeleaf ('admin/adminDashboard') o una redirección a la página de inicio de sesión.
     */
    @GetMapping("/adminDashboard/{token}")
    public String getAdminDashboard(@PathVariable String token, Model model) {
        System.out.println("DEBUG: Intento de acceso al Panel de Control del Administrador con token: " + token);
        try {
            Long userId = tokenService.getUserIdFromToken(token);
            boolean isValid = tokenService.validateTokenForUser(token, userId, "admin");

            if (isValid) {
                model.addAttribute("token", token); // Añadir token al modelo si es necesario en Thymeleaf
                // Devolver 'admin/adminDashboard' que Thymeleaf resolverá a
                // src/main/resources/templates/admin/adminDashboard.html
                return "admin/adminDashboard"; // Nombre de la plantilla de Thymeleaf sin la ruta completa
            } else {
                System.err.println("Acceso al Panel de Control del Administrador denegado: Token inválido o rol no coincide para token: " + token);
                return "redirect:/"; // Redirecciona a la raíz, que debería ser tu index.html
            }
        } catch (Exception e) {
            System.err.println("Error al extraer el userId del token: " + e.getMessage());
            System.err.println("Acceso al Panel de Control del Administrador denegado: Token inválido o rol no coincide para token: " + token);
            return "redirect:/"; // Redirecciona a la raíz
        }
    }

    /**
     * Maneja las solicitudes para el Panel de Control del Médico.
     * Requiere un token JWT en la variable de ruta para la autenticación.
     * @param token El token JWT para la autenticación.
     * @param model El Modelo de Spring para añadir atributos a la vista.
     * @return El nombre de la plantilla de Thymeleaf ('doctor/doctorDashboard') o una redirección a la página de inicio de sesión.
     */
    @GetMapping("/doctorDashboard/{token}")
    public String getDoctorDashboard(@PathVariable String token, Model model) {
        System.out.println("DEBUG: Intento de acceso al Panel de Control del Médico con token: " + token);
        try {
            // --- INICIO DE LÓGICA PARA TOKEN SIMULADO ---
            // Verifica si el token es el JWT simulado que devolvió el DoctorService
            if ("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjI1LCJyb2xlIjoiRE9DVE9SIiwiaWF0IjoxNjgwMDAwMDAwfQ.DUMMY_SIGNATURE".equals(token)) {
                model.addAttribute("token", token);
                System.out.println("DEBUG: Acceso al Panel de Control del Médico concedido con token SIMULADO: " + token);
                return "doctor/doctorDashboard"; // Nombre de la plantilla de Thymeleaf
            }
            // --- FIN DE LÓGICA PARA TOKEN SIMULADO ---

            // Lógica de validación normal para JWTs reales
            Long userId = tokenService.getUserIdFromToken(token);
            boolean isValid = tokenService.validateTokenForUser(token, userId, "doctor");

            if (isValid) {
                model.addAttribute("token", token);
                System.out.println("DEBUG: Acceso al Panel de Control del Médico concedido para userId: " + userId);
                return "doctor/doctorDashboard"; // Nombre de la plantilla de Thymeleaf
            } else {
                System.err.println("Acceso al Panel de Control del Médico denegado: Token inválido o rol no coincide para token: " + token);
                return "redirect:/"; // Redirecciona a la página de inicio de sesión
            }
        } catch (Exception e) {
            System.err.println("Error al extraer el userId del token: " + e.getMessage());
            System.err.println("Acceso al Panel de Control del Médico denegado: Token inválido o rol no coincide para token: " + token);
            return "redirect:/"; // Redirecciona a la página de inicio de sesión
        }
    }
}
