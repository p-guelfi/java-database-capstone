package com.project.back_end.repository.mysql;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Doctor.
 * Extiende JpaRepository para heredar operaciones CRUD estándar y
 * define métodos de consulta personalizados para buscar y filtrar doctores.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Encuentra un Doctor por su dirección de email.
     * @param email La dirección de email del doctor.
     * @return Un Optional que contiene el Doctor si se encuentra, o un Optional vacío.
     */
    Optional<Doctor> findByEmail(String email);

    /**
     * Encuentra un Doctor por su dirección de email o número de teléfono.
     * Esto es útil para validar la unicidad antes de guardar un nuevo doctor.
     * @param email La dirección de email del doctor.
     * @param phone El número de teléfono del doctor.
     * @return Un Optional que contiene un Doctor si se encuentra una coincidencia por email o teléfono, o un Optional vacío.
     */
    Optional<Doctor> findByEmailOrPhone(String email, String phone);

    /**
     * Busca doctores cuyo nombre contenga la cadena dada (ignorando mayúsculas y minúsculas).
     * @param name El nombre (o parte del nombre) a buscar.
     * @return Una lista de doctores que coinciden con el criterio.
     */
    List<Doctor> findByNameContainingIgnoreCase(String name);

    /**
     * Busca doctores cuya especialidad contenga la cadena dada (ignorando mayúsculas y minúsculas).
     * @param specialty La especialidad (o parte de la especialidad) a buscar.
     * @return Una lista de doctores que coinciden con el criterio.
     */
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);

    /**
     * Busca doctores cuyo nombre Y especialidad contengan las cadenas dadas (ignorando mayúsculas y minúsculas).
     * @param name El nombre (o parte del nombre) a buscar.
     * @param specialty La especialidad (o parte de la especialidad) a buscar.
     * @return Una lista de doctores que coinciden con ambos criterios.
     */
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(String name, String specialty);
}
