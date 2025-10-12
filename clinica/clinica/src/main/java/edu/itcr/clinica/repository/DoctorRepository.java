package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @EntityGraph(attributePaths = "especialidades")
    Optional<Doctor> findByIdDoctor(Long idDoctor);
}
