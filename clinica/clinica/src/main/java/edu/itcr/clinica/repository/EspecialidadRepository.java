package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    // Buscar especialidad por nombre ignorando mayúsculas/minúsculas
    Optional<Especialidad> findByNomEspecialidadIgnoreCase(String nomEspecialidad);

    // Obtener todas las especialidades de un doctor específico
    List<Especialidad> findByDoctores_IdDoctor(Long idDoctor);
}
