package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    Optional<Especialidad> findByNomEspecialidadIgnoreCase(String nomEspecialidad);
    List<Especialidad> findByDoctores_IdDoctor(Long idDoctor);
}
